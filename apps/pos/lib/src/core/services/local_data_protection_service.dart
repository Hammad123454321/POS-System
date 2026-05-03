import 'local_encryption_key_store.dart';

abstract interface class DatabaseKeyRotator {
  Future<void> rotateEncryptionKey(String newKey);
}

class LocalDataProtectionService {
  LocalDataProtectionService({
    required LocalEncryptionKeyStore keyStore,
    required DatabaseKeyRotator database,
    DateTime Function()? clock,
  }) : _keyStore = keyStore,
       _database = database,
       _clock = clock ?? (() => DateTime.now().toUtc());

  final LocalEncryptionKeyStore _keyStore;
  final DatabaseKeyRotator _database;
  final DateTime Function() _clock;

  Future<bool> rotateIfDue() async {
    final state = await _keyStore.loadOrCreateState();

    if (!state.isScheduledRotationDue(_clock().toUtc())) {
      return false;
    }

    await _performRotation(reason: 'scheduled_90_day');

    return true;
  }

  Future<void> rotateForTrigger(String reason) async {
    await _performRotation(reason: reason);
  }

  Future<void> _performRotation({required String reason}) async {
    final state = await _keyStore.beginRotation(reason: reason);
    final pending = state.pending;

    if (pending == null) {
      return;
    }

    try {
      await _database.rotateEncryptionKey(pending.key);
      await _keyStore.commitRotation();
    } catch (_) {
      await _keyStore.cancelRotation();
      rethrow;
    }
  }
}
