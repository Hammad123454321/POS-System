import 'package:flutter/material.dart';

import '../../app/pos_theme.dart';
import '../home/pos_home_controller.dart';

/// Full-screen lock shown when the device's authorization has been revoked.
/// Preserves queued data, shows the quarantined-event count, and offers
/// re-enrollment. After re-enrolling, if held events remain, a manager-approval
/// step is shown.
class DeviceLockScreen extends StatefulWidget {
  const DeviceLockScreen({required this.controller, super.key});

  final PosHomeController controller;

  @override
  State<DeviceLockScreen> createState() => _DeviceLockScreenState();
}

class _DeviceLockScreenState extends State<DeviceLockScreen> {
  late final TextEditingController _urlController;
  final _codeController = TextEditingController();
  final _nameController = TextEditingController(text: 'Front Register');

  @override
  void initState() {
    super.initState();
    _urlController = TextEditingController(
      text:
          widget.controller.bootstrapSnapshot?.apiBaseUrl ??
          const String.fromEnvironment('API_BASE_URL', defaultValue: ''),
    );
  }

  @override
  void dispose() {
    _urlController.dispose();
    _codeController.dispose();
    _nameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final held = widget.controller.authHoldSyncEvents;

        return PopScope(
          canPop: false,
          child: Scaffold(
            backgroundColor: PosColors.forest,
            body: SafeArea(
              child: Center(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.lock, color: Colors.white, size: 64),
                      const SizedBox(height: 16),
                      const Text(
                        'Device authorization revoked',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 22,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        held > 0
                            ? '$held queued events are quarantined and preserved on this device.'
                            : 'Local data is preserved. Re-enroll to continue.',
                        textAlign: TextAlign.center,
                        style: const TextStyle(color: Colors.white70),
                      ),
                      const SizedBox(height: 24),
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            children: [
                              TextField(
                                controller: _urlController,
                                decoration: const InputDecoration(
                                  labelText: 'API base URL',
                                ),
                              ),
                              TextField(
                                controller: _nameController,
                                decoration: const InputDecoration(
                                  labelText: 'Device name',
                                ),
                              ),
                              TextField(
                                controller: _codeController,
                                decoration: const InputDecoration(
                                  labelText: 'Enrollment code',
                                ),
                              ),
                              const SizedBox(height: 12),
                              SizedBox(
                                width: double.infinity,
                                child: FilledButton(
                                  onPressed: widget.controller.isBusy
                                      ? null
                                      : () => widget.controller.enroll(
                                          apiBaseUrl: _urlController.text,
                                          enrollmentCode: _codeController.text,
                                          deviceName: _nameController.text,
                                        ),
                                  child: const Text('Re-enroll device'),
                                ),
                              ),
                              if (held > 0) ...[
                                const Divider(),
                                const Text(
                                  'A manager must approve the quarantined events.',
                                  textAlign: TextAlign.center,
                                ),
                                const SizedBox(height: 8),
                                SizedBox(
                                  width: double.infinity,
                                  child: OutlinedButton(
                                    onPressed: widget.controller.isBusy
                                        ? null
                                        : widget
                                              .controller
                                              .approveHeldSyncEvents,
                                    child: const Text(
                                      'Manager: approve held events',
                                    ),
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                      ),
                      if (widget.controller.errorMessage != null) ...[
                        const SizedBox(height: 16),
                        Text(
                          widget.controller.errorMessage!,
                          style: const TextStyle(color: Colors.amberAccent),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}
