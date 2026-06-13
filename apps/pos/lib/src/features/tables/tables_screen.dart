import 'package:flutter/material.dart';

import '../../app/pos_theme.dart';
import '../../core/models/pos_models.dart';
import '../home/pos_home_controller.dart';

/// Visual floor plan: dining tables grouped by zone, each tile colored by
/// lease/occupancy state. Tap an available table to claim it, or a table you
/// hold to open its order or release it.
class TablesScreen extends StatelessWidget {
  const TablesScreen({required this.controller, this.onOpenOrder, super.key});

  final PosHomeController controller;

  /// Invoked when the user chooses "Open order" for a table they hold; the
  /// shell switches to the Register tab.
  final VoidCallback? onOpenOrder;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final tables = controller.diningTables;
        if (tables.isEmpty) {
          return const Center(child: Text('No dining tables configured.'));
        }

        final byZone = <String, List<DiningTableSnapshot>>{};
        for (final table in tables) {
          byZone.putIfAbsent(table.zoneName ?? 'Floor', () => []).add(table);
        }
        final zones = byZone.keys.toList()..sort();

        return RefreshIndicator(
          onRefresh: controller.refreshCloudState,
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              for (final zone in zones) ...[
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  child: Text(
                    zone,
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                ),
                GridView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  gridDelegate: const SliverGridDelegateWithMaxCrossAxisExtent(
                    maxCrossAxisExtent: 160,
                    childAspectRatio: 1,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                  ),
                  itemCount: byZone[zone]!.length,
                  itemBuilder: (context, i) => _TableTile(
                    table: byZone[zone]![i],
                    onTap: () => _onTap(context, byZone[zone]![i]),
                  ),
                ),
              ],
            ],
          ),
        );
      },
    );
  }

  Future<void> _onTap(BuildContext context, DiningTableSnapshot table) async {
    final state = _stateOf(table);
    switch (state) {
      case _TableState.available:
        final claim = await _claimDialog(context);
        if (claim != null) {
          await controller.claimDiningTable(
            diningTableId: table.id,
            currentPartyName: claim.party,
            guestCount: claim.guests,
          );
        }
        break;
      case _TableState.mine:
        if (!context.mounted) return;
        await showModalBottomSheet<void>(
          context: context,
          builder: (sheetContext) => SafeArea(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                ListTile(
                  leading: const Icon(Icons.point_of_sale),
                  title: const Text('Open order'),
                  onTap: () {
                    Navigator.pop(sheetContext);
                    onOpenOrder?.call();
                  },
                ),
                ListTile(
                  leading: const Icon(Icons.logout),
                  title: const Text('Release table'),
                  onTap: () {
                    Navigator.pop(sheetContext);
                    controller.releaseDiningTable(table.id);
                  },
                ),
              ],
            ),
          ),
        );
        break;
      case _TableState.other:
      case _TableState.occupied:
        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('${table.name} is in use on another device.')),
        );
        break;
    }
  }

  Future<_ClaimInput?> _claimDialog(BuildContext context) {
    final party = TextEditingController();
    final guests = TextEditingController();
    return showDialog<_ClaimInput>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Claim table'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: party,
              decoration: const InputDecoration(labelText: 'Party name'),
            ),
            TextField(
              controller: guests,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(labelText: 'Guests'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(
              context,
              _ClaimInput(
                party: party.text.trim().isEmpty ? null : party.text.trim(),
                guests: int.tryParse(guests.text.trim()),
              ),
            ),
            child: const Text('Claim'),
          ),
        ],
      ),
    );
  }
}

enum _TableState { available, mine, other, occupied }

_TableState _stateOf(DiningTableSnapshot table) {
  if (table.lease.isClaimedByCurrentDevice) return _TableState.mine;
  if (table.lease.currentHolderDeviceId != null) return _TableState.other;
  if (table.status == 'occupied') return _TableState.occupied;
  return _TableState.available;
}

class _ClaimInput {
  _ClaimInput({this.party, this.guests});
  final String? party;
  final int? guests;
}

class _TableTile extends StatelessWidget {
  const _TableTile({required this.table, required this.onTap});

  final DiningTableSnapshot table;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final state = _stateOf(table);
    final (color, filled, icon) = switch (state) {
      _TableState.mine => (PosColors.tableClaimedByMe, true, Icons.check),
      _TableState.other => (PosColors.tableClaimedOther, true, Icons.lock),
      _TableState.occupied => (PosColors.tableOccupied, false, Icons.people),
      _TableState.available => (PosColors.tableAvailable, false, null),
    };

    return InkWell(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: filled ? color : color.withValues(alpha: 0.18),
          border: Border.all(color: color, width: 2),
          borderRadius: BorderRadius.circular(12),
        ),
        padding: const EdgeInsets.all(8),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (icon != null)
              Icon(icon, color: filled ? Colors.white : color, size: 20),
            Text(
              table.name,
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: filled ? Colors.white : Colors.black87,
              ),
            ),
            Text(
              'Seats ${table.capacity}',
              style: TextStyle(
                fontSize: 11,
                color: filled ? Colors.white70 : Colors.black54,
              ),
            ),
            if (table.currentPartyName != null)
              Text(
                table.currentPartyName!,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  fontSize: 11,
                  color: filled ? Colors.white : Colors.black87,
                ),
              ),
          ],
        ),
      ),
    );
  }
}
