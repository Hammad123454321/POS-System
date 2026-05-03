import 'dart:convert';

import 'package:drift/drift.dart';

import '../local/pos_database.dart';

class SyncOutboxEntryData {
  const SyncOutboxEntryData({
    required this.localEventId,
    required this.entityType,
    required this.action,
    required this.payload,
    required this.status,
    required this.attempts,
    required this.availableAt,
  });

  final String localEventId;
  final String entityType;
  final String action;
  final Map<String, dynamic> payload;
  final String status;
  final int attempts;
  final DateTime availableAt;
}

class SyncOutboxRepository {
  const SyncOutboxRepository(this._database);

  final PosDatabase _database;

  Future<int> pendingCount() async {
    return _countForStatus('pending');
  }

  Future<int> authHoldCount() async {
    return _countForStatus('auth_hold');
  }

  Future<void> enqueue({
    required String localEventId,
    required String entityType,
    required String action,
    required Map<String, dynamic> payload,
  }) async {
    await _database
        .into(_database.syncOutboxEntries)
        .insertOnConflictUpdate(
          SyncOutboxEntriesCompanion.insert(
            localEventId: localEventId,
            entityType: entityType,
            action: action,
            payloadJson: jsonEncode(payload),
            createdAt: DateTime.now().toUtc(),
            availableAt: DateTime.now().toUtc(),
          ),
        );
  }

  Future<List<SyncOutboxEntryData>> pending({int limit = 50}) async {
    final rows =
        await (_database.select(_database.syncOutboxEntries)
              ..where(
                (table) =>
                    table.status.equals('pending') &
                    table.availableAt.isSmallerOrEqualValue(
                      DateTime.now().toUtc(),
                    ),
              )
              ..orderBy([(table) => OrderingTerm.asc(table.createdAt)])
              ..limit(limit))
            .get();

    return rows
        .map(
          (row) => SyncOutboxEntryData(
            localEventId: row.localEventId,
            entityType: row.entityType,
            action: row.action,
            payload: jsonDecode(row.payloadJson) as Map<String, dynamic>,
            status: row.status,
            attempts: row.attempts,
            availableAt: row.availableAt,
          ),
        )
        .toList(growable: false);
  }

  Future<void> markSynced(Iterable<String> localEventIds) async {
    if (localEventIds.isEmpty) {
      return;
    }

    await (_database.update(_database.syncOutboxEntries)
          ..where((table) => table.localEventId.isIn(localEventIds.toList())))
        .write(const SyncOutboxEntriesCompanion(status: Value('synced')));
  }

  Future<void> movePendingToAuthHold() async {
    await (_database.update(_database.syncOutboxEntries)
          ..where((table) => table.status.equals('pending')))
        .write(const SyncOutboxEntriesCompanion(status: Value('auth_hold')));
  }

  Future<void> releaseAuthHoldToPending() async {
    await (_database.update(
      _database.syncOutboxEntries,
    )..where((table) => table.status.equals('auth_hold'))).write(
      SyncOutboxEntriesCompanion(
        status: const Value('pending'),
        availableAt: Value(DateTime.now().toUtc()),
      ),
    );
  }

  Future<int> _countForStatus(String status) async {
    final result = await _database
        .customSelect(
          'SELECT COUNT(*) AS item_count FROM sync_outbox_entries WHERE status = ?',
          variables: [Variable<String>(status)],
        )
        .getSingle();

    return result.read<int>('item_count');
  }
}
