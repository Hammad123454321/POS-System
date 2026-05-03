import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/core/services/local_data_protection_service.dart';
import 'package:pos_app/src/core/services/local_encryption_key_store.dart';

void main() {
  test('rotateIfDue rekeys and commits after 90 days', () async {
    final backend = _InMemoryLocalEncryptionBackend();
    final keyStore = LocalEncryptionKeyStore(
      backend: backend,
      clock: () => DateTime.utc(2026, 1, 1),
    );

    await keyStore.loadOrCreateState();

    final service = LocalDataProtectionService(
      keyStore: LocalEncryptionKeyStore(
        backend: backend,
        clock: () => DateTime.utc(2026, 4, 2),
      ),
      database: _FakeDatabaseKeyRotator(),
      clock: () => DateTime.utc(2026, 4, 2),
    );

    final rotated = await service.rotateIfDue();
    final state = await keyStore.loadOrCreateState();

    expect(rotated, isTrue);
    expect(state.active.version, 2);
    expect(state.pending, isNull);
  });

  test('rotateForTrigger cancels pending key when rekey fails', () async {
    final backend = _InMemoryLocalEncryptionBackend();
    final keyStore = LocalEncryptionKeyStore(
      backend: backend,
      clock: () => DateTime.utc(2026, 4, 2),
    );
    final service = LocalDataProtectionService(
      keyStore: keyStore,
      database: _FakeDatabaseKeyRotator(throwOnRotate: true),
      clock: () => DateTime.utc(2026, 4, 2),
    );

    await keyStore.loadOrCreateState();

    await expectLater(
      () => service.rotateForTrigger('device_auth_revoked'),
      throwsA(isA<StateError>()),
    );

    final state = await keyStore.loadOrCreateState();
    expect(state.active.version, 1);
    expect(state.pending, isNull);
  });
}

class _FakeDatabaseKeyRotator implements DatabaseKeyRotator {
  _FakeDatabaseKeyRotator({this.throwOnRotate = false});

  final bool throwOnRotate;
  final List<String> rotatedKeys = <String>[];

  @override
  Future<void> rotateEncryptionKey(String newKey) async {
    if (throwOnRotate) {
      throw StateError('rekey failed');
    }

    rotatedKeys.add(newKey);
  }
}

class _InMemoryLocalEncryptionBackend implements LocalEncryptionBackend {
  LocalEncryptionKeyState? _state;

  @override
  Future<LocalEncryptionKeyState> beginRotation({
    required String reason,
    required DateTime now,
  }) async {
    final current = await loadOrCreateState(now: now);

    if (current.pending != null) {
      return current;
    }

    _state = LocalEncryptionKeyState(
      active: current.active,
      pending: LocalEncryptionKeyMaterial(
        key: 'pending-key-$reason',
        version: current.active.version + 1,
        createdAt: now,
        reason: reason,
      ),
    );

    return _state!;
  }

  @override
  Future<LocalEncryptionKeyState> cancelRotation() async {
    final current = _state!;
    _state = LocalEncryptionKeyState(active: current.active);

    return _state!;
  }

  @override
  Future<LocalEncryptionKeyState> commitRotation() async {
    final current = _state!;
    _state = LocalEncryptionKeyState(active: current.pending ?? current.active);

    return _state!;
  }

  @override
  Future<LocalEncryptionKeyState> loadOrCreateState({
    required DateTime now,
  }) async {
    _state ??= LocalEncryptionKeyState(
      active: LocalEncryptionKeyMaterial(
        key: 'active-key',
        version: 1,
        createdAt: now,
      ),
    );

    return _state!;
  }
}
