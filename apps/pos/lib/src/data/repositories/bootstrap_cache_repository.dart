import 'dart:convert';

import 'package:drift/drift.dart';

import '../local/pos_database.dart';

class BootstrapCacheSnapshot {
  const BootstrapCacheSnapshot({
    required this.id,
    required this.apiBaseUrl,
    required this.deviceId,
    required this.merchantId,
    required this.storeId,
    required this.payload,
    required this.syncedAt,
  });

  final String id;
  final String? apiBaseUrl;
  final String? deviceId;
  final String? merchantId;
  final String? storeId;
  final Map<String, dynamic> payload;
  final DateTime syncedAt;
}

class BootstrapCacheRepository {
  const BootstrapCacheRepository(this._database);

  final PosDatabase _database;

  Future<void> replace({
    required String apiBaseUrl,
    required String deviceId,
    required String merchantId,
    required String storeId,
    required Map<String, dynamic> payload,
  }) async {
    await _database
        .into(_database.deviceBootstrapCaches)
        .insertOnConflictUpdate(
          DeviceBootstrapCachesCompanion.insert(
            id: 'active-bootstrap',
            apiBaseUrl: Value(apiBaseUrl),
            deviceId: Value(deviceId),
            merchantId: Value(merchantId),
            storeId: Value(storeId),
            payloadJson: jsonEncode(payload),
            syncedAt: DateTime.now().toUtc(),
          ),
        );
  }

  Future<BootstrapCacheSnapshot?> latest() async {
    final row =
        await (_database.select(_database.deviceBootstrapCaches)
              ..orderBy([
                (table) => OrderingTerm(
                  expression: table.syncedAt,
                  mode: OrderingMode.desc,
                ),
              ])
              ..limit(1))
            .getSingleOrNull();

    if (row == null) {
      return null;
    }

    return BootstrapCacheSnapshot(
      id: row.id,
      apiBaseUrl: row.apiBaseUrl,
      deviceId: row.deviceId,
      merchantId: row.merchantId,
      storeId: row.storeId,
      payload: jsonDecode(row.payloadJson) as Map<String, dynamic>,
      syncedAt: row.syncedAt,
    );
  }

  Future<void> clear() async {
    await _database.delete(_database.deviceBootstrapCaches).go();
  }
}
