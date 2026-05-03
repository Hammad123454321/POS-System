import 'dart:convert';

import 'package:drift/drift.dart';

import '../local/pos_database.dart';

class ReceiptPrintJobData {
  const ReceiptPrintJobData({
    required this.id,
    required this.receiptId,
    required this.routeKey,
    required this.payload,
  });

  final String id;
  final String? receiptId;
  final String routeKey;
  final Map<String, dynamic> payload;
}

class ReceiptPrintQueueRepository {
  const ReceiptPrintQueueRepository(this._database);

  final PosDatabase _database;

  Future<int> pendingCount() async {
    final result = await _database
        .customSelect(
          'SELECT COUNT(*) AS item_count FROM receipt_print_jobs WHERE status = ?',
          variables: [Variable<String>('pending')],
        )
        .getSingle();

    return result.read<int>('item_count');
  }

  Future<void> enqueue({
    required String id,
    required String receiptId,
    required String routeKey,
    required Map<String, dynamic> payload,
  }) async {
    await _database
        .into(_database.receiptPrintJobs)
        .insertOnConflictUpdate(
          ReceiptPrintJobsCompanion.insert(
            id: id,
            receiptId: Value(receiptId),
            routeKey: routeKey,
            payloadJson: jsonEncode(payload),
            createdAt: DateTime.now().toUtc(),
            expiresAt: DateTime.now().toUtc().add(const Duration(days: 7)),
          ),
        );
  }

  Future<List<ReceiptPrintJobData>> pending({int limit = 20}) async {
    final rows =
        await (_database.select(_database.receiptPrintJobs)
              ..where(
                (table) =>
                    table.status.equals('pending') &
                    table.expiresAt.isBiggerOrEqualValue(
                      DateTime.now().toUtc(),
                    ),
              )
              ..orderBy([(table) => OrderingTerm.asc(table.createdAt)])
              ..limit(limit))
            .get();

    return rows
        .map(
          (row) => ReceiptPrintJobData(
            id: row.id,
            receiptId: row.receiptId,
            routeKey: row.routeKey,
            payload: jsonDecode(row.payloadJson) as Map<String, dynamic>,
          ),
        )
        .toList(growable: false);
  }

  Future<void> markCompleted(String id) async {
    await (_database.update(_database.receiptPrintJobs)
          ..where((table) => table.id.equals(id)))
        .write(const ReceiptPrintJobsCompanion(status: Value('completed')));
  }
}
