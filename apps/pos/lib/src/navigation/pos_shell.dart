import 'package:flutter/material.dart';

import '../app/pos_theme.dart';
import '../features/appointments/appointments_screen.dart';
import '../features/checkout/register_screen.dart';
import '../features/checkout/tender_screen.dart';
import '../features/home/pos_home_screen.dart';
import '../features/home/pos_home_controller.dart';
import '../features/security/device_lock_screen.dart';
import '../features/tables/tables_screen.dart';

/// Top-level navigation surface. Branches on controller state:
///  - loading            -> splash
///  - !isEnrolled         -> enrollment (handled by the embedded home screen,
///                           which already renders the enrollment card)
///  - requiresReauth      -> device lock (B-9; until then the embedded home
///                           screen surfaces the re-enrollment card)
///  - otherwise           -> tabbed operating surface
///
/// Wide layouts (>= 840dp) use a NavigationRail; narrow use a bottom
/// NavigationBar. The legacy console survives as the "Operations" tab via
/// PosHomeScreen(embedded: true).
class PosShell extends StatefulWidget {
  const PosShell({required this.controller, super.key});

  final PosHomeController controller;

  @override
  State<PosShell> createState() => _PosShellState();
}

class _PosShellState extends State<PosShell> {
  int _index = 0;

  static const _destinations = <_ShellDestination>[
    _ShellDestination('Register', Icons.point_of_sale),
    _ShellDestination('Tables', Icons.table_restaurant),
    _ShellDestination('Appointments', Icons.event),
    _ShellDestination('Operations', Icons.dashboard_customize),
  ];

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final controller = widget.controller;

        if (controller.isLoading && controller.bootstrapSnapshot == null) {
          return const _SplashScreen();
        }

        // Revoked device → full-screen lock takes over the entire UI.
        if (controller.requiresReauth) {
          return DeviceLockScreen(controller: controller);
        }

        // Until the dedicated register/tables/appointments + lock screens land,
        // the embedded console covers enrollment, reauth, and operations. The
        // shell still provides the navigation frame around it.
        final tabs = <Widget>[
          controller.isEnrolled
              ? RegisterScreen(
                  controller: controller,
                  onCharge: () => _onCharge(context, controller),
                )
              : const _PlaceholderTab(
                  title: 'Register',
                  message:
                      'Enroll this device from the Operations tab to begin.',
                ),
          controller.isEnrolled
              ? TablesScreen(
                  controller: controller,
                  onOpenOrder: () => setState(() => _index = 0),
                )
              : const _PlaceholderTab(
                  title: 'Tables',
                  message: 'Enroll this device to manage tables.',
                ),
          controller.isEnrolled
              ? AppointmentsScreen(controller: controller)
              : const _PlaceholderTab(
                  title: 'Appointments',
                  message: 'Enroll this device to manage appointments.',
                ),
          PosHomeScreen(controller: controller, embedded: true),
        ];

        final isWide = MediaQuery.sizeOf(context).width >= 840;
        final content = IndexedStack(index: _index, children: tabs);

        return Scaffold(
          backgroundColor: PosColors.cream,
          appBar: AppBar(
            backgroundColor: PosColors.forest,
            foregroundColor: Colors.white,
            title: Text(_destinations[_index].label),
            actions: [
              _SyncBadge(pending: controller.pendingSyncEvents),
              IconButton(
                tooltip: 'Reload',
                onPressed: controller.isBusy
                    ? null
                    : (controller.isEnrolled
                          ? controller.refreshCloudState
                          : controller.load),
                icon: const Icon(Icons.refresh),
              ),
            ],
          ),
          body: isWide
              ? Row(
                  children: [
                    NavigationRail(
                      selectedIndex: _index,
                      onDestinationSelected: (i) => setState(() => _index = i),
                      labelType: NavigationRailLabelType.all,
                      destinations: [
                        for (final d in _destinations)
                          NavigationRailDestination(
                            icon: Icon(d.icon),
                            label: Text(d.label),
                          ),
                      ],
                    ),
                    const VerticalDivider(width: 1),
                    Expanded(child: content),
                  ],
                )
              : content,
          bottomNavigationBar: isWide
              ? null
              : NavigationBar(
                  selectedIndex: _index,
                  onDestinationSelected: (i) => setState(() => _index = i),
                  destinations: [
                    for (final d in _destinations)
                      NavigationDestination(icon: Icon(d.icon), label: d.label),
                  ],
                ),
        );
      },
    );
  }
}

Future<void> _onCharge(
  BuildContext context,
  PosHomeController controller,
) async {
  // No open register yet → prompt for an opening float.
  if (controller.activeRegisterSession == null) {
    final float = await _promptAmount(
      context,
      'Open Register',
      'Opening float',
    );
    if (float != null) {
      await controller.openRegister(float);
    }
    return;
  }

  // Open the full tender flow.
  await Navigator.of(context).push(
    MaterialPageRoute(builder: (_) => TenderScreen(controller: controller)),
  );
}

Future<int?> _promptAmount(BuildContext context, String title, String label) {
  final controller = TextEditingController();
  return showDialog<int>(
    context: context,
    builder: (context) => AlertDialog(
      title: Text(title),
      content: TextField(
        controller: controller,
        keyboardType: TextInputType.number,
        autofocus: true,
        decoration: InputDecoration(labelText: '$label (minor units)'),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: () {
            final value = int.tryParse(controller.text.trim());
            Navigator.pop(context, value);
          },
          child: const Text('Confirm'),
        ),
      ],
    ),
  );
}

class _ShellDestination {
  const _ShellDestination(this.label, this.icon);
  final String label;
  final IconData icon;
}

class _SplashScreen extends StatelessWidget {
  const _SplashScreen();

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: PosColors.cream,
      body: Center(child: CircularProgressIndicator()),
    );
  }
}

class _SyncBadge extends StatelessWidget {
  const _SyncBadge({required this.pending});
  final int pending;

  @override
  Widget build(BuildContext context) {
    if (pending <= 0) {
      return const SizedBox.shrink();
    }
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: Center(
        child: Chip(
          label: Text('$pending queued'),
          backgroundColor: PosColors.tableClaimedOther,
          labelStyle: const TextStyle(color: Colors.white, fontSize: 12),
        ),
      ),
    );
  }
}

class _PlaceholderTab extends StatelessWidget {
  const _PlaceholderTab({required this.title, required this.message});
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(title, style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 12),
            Text(message, textAlign: TextAlign.center),
          ],
        ),
      ),
    );
  }
}
