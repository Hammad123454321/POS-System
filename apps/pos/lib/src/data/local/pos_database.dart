import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:sqlite3/sqlite3.dart' show Database;

import '../../core/services/local_data_protection_service.dart';
import '../../core/services/local_encryption_key_store.dart';

part 'pos_database.g.dart';

class DeviceBootstrapCaches extends Table {
  TextColumn get id => text()();

  TextColumn get apiBaseUrl => text().nullable()();

  TextColumn get deviceId => text().nullable()();

  TextColumn get merchantId => text().nullable()();

  TextColumn get storeId => text().nullable()();

  TextColumn get payloadJson => text()();

  DateTimeColumn get syncedAt => dateTime()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

class SyncOutboxEntries extends Table {
  TextColumn get localEventId => text()();

  TextColumn get entityType => text()();

  TextColumn get action => text()();

  TextColumn get payloadJson => text()();

  TextColumn get status => text().withDefault(const Constant('pending'))();

  IntColumn get attempts => integer().withDefault(const Constant(0))();

  DateTimeColumn get createdAt => dateTime()();

  DateTimeColumn get availableAt => dateTime()();

  @override
  Set<Column<Object>> get primaryKey => {localEventId};
}

class ReceiptPrintJobs extends Table {
  TextColumn get id => text()();

  TextColumn get receiptId => text().nullable()();

  TextColumn get routeKey => text()();

  TextColumn get payloadJson => text()();

  TextColumn get status => text().withDefault(const Constant('pending'))();

  DateTimeColumn get createdAt => dateTime()();

  DateTimeColumn get expiresAt => dateTime()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

@DriftDatabase(
  tables: [DeviceBootstrapCaches, SyncOutboxEntries, ReceiptPrintJobs],
)
class PosDatabase extends _$PosDatabase implements DatabaseKeyRotator {
  PosDatabase._(super.executor);

  factory PosDatabase.memory() {
    return PosDatabase._(
      NativeDatabase.memory(
        setup: (rawDb) {
          rawDb.execute('PRAGMA foreign_keys = ON;');
        },
      ),
    );
  }

  static Future<PosDatabase> open(LocalEncryptionKeyStore keyStore) async {
    final appSupportDirectory = await getApplicationSupportDirectory();
    final databaseFile = File(
      p.join(appSupportDirectory.path, 'pos_device.sqlite'),
    );
    final keyState = await keyStore.loadOrCreateState();

    try {
      return await _openWithKey(databaseFile, keyState.active.key);
    } catch (_) {
      final pending = keyState.pending;

      if (pending == null) {
        rethrow;
      }

      final recovered = await _openWithKey(databaseFile, pending.key);
      await keyStore.commitRotation();

      return recovered;
    }
  }

  @override
  int get schemaVersion => 1;

  @override
  Future<void> rotateEncryptionKey(String newKey) async {
    await customStatement('PRAGMA wal_checkpoint(FULL);');
    await customStatement("PRAGMA rekey = '${_escapeForPragma(newKey)}';");
    await customSelect(
      'SELECT COUNT(*) AS item_count FROM sqlite_master;',
    ).getSingle();
  }

  static Future<PosDatabase> _openWithKey(File databaseFile, String key) async {
    final database = PosDatabase._(
      NativeDatabase.createInBackground(
        databaseFile,
        setup: (rawDb) {
          assert(_debugCheckHasCipher(rawDb));
          rawDb.execute("PRAGMA key = '${_escapeForPragma(key)}';");
          rawDb.execute('PRAGMA foreign_keys = ON;');
          rawDb.execute('PRAGMA journal_mode = WAL;');
        },
      ),
    );

    try {
      await database
          .customSelect('SELECT COUNT(*) AS item_count FROM sqlite_master;')
          .getSingle();

      return database;
    } catch (_) {
      await database.close();
      rethrow;
    }
  }

  static bool _debugCheckHasCipher(Database database) {
    return database.select('PRAGMA cipher;').isNotEmpty;
  }

  static String _escapeForPragma(String value) {
    return value.replaceAll("'", "''");
  }
}
