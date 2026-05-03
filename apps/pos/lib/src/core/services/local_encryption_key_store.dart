import 'dart:convert';
import 'dart:io';
import 'dart:math';

import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class LocalEncryptionKeyMaterial {
  const LocalEncryptionKeyMaterial({
    required this.key,
    required this.version,
    required this.createdAt,
    this.reason,
  });

  final String key;
  final int version;
  final DateTime createdAt;
  final String? reason;

  factory LocalEncryptionKeyMaterial.fromJson(Map<String, dynamic> json) {
    return LocalEncryptionKeyMaterial(
      key: json['key'] as String,
      version: json['version'] as int,
      createdAt: DateTime.fromMillisecondsSinceEpoch(
        json['created_at_ms'] as int,
        isUtc: true,
      ),
      reason: json['reason'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'key': key,
      'version': version,
      'created_at_ms': createdAt.toUtc().millisecondsSinceEpoch,
      if (reason != null && reason!.isNotEmpty) 'reason': reason,
    };
  }
}

class LocalEncryptionKeyState {
  const LocalEncryptionKeyState({required this.active, this.pending});

  final LocalEncryptionKeyMaterial active;
  final LocalEncryptionKeyMaterial? pending;

  bool get hasPendingRotation => pending != null;

  bool isScheduledRotationDue(DateTime now) {
    return now.toUtc().difference(active.createdAt) >= const Duration(days: 90);
  }

  factory LocalEncryptionKeyState.fromJson(Map<String, dynamic> json) {
    final pendingPayload = json['pending'];

    return LocalEncryptionKeyState(
      active: LocalEncryptionKeyMaterial.fromJson(
        (json['active'] as Map).cast<String, dynamic>(),
      ),
      pending: pendingPayload == null
          ? null
          : LocalEncryptionKeyMaterial.fromJson(
              (pendingPayload as Map).cast<String, dynamic>(),
            ),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'active': active.toJson(),
      if (pending != null) 'pending': pending!.toJson(),
    };
  }
}

abstract interface class LocalEncryptionBackend {
  Future<LocalEncryptionKeyState> loadOrCreateState({required DateTime now});
  Future<LocalEncryptionKeyState> beginRotation({
    required String reason,
    required DateTime now,
  });
  Future<LocalEncryptionKeyState> commitRotation();
  Future<LocalEncryptionKeyState> cancelRotation();
}

class LocalEncryptionKeyStore {
  LocalEncryptionKeyStore({
    FlutterSecureStorage? storage,
    MethodChannel? deviceSecurityChannel,
    LocalEncryptionBackend? backend,
    DateTime Function()? clock,
  }) : _clock = clock ?? (() => DateTime.now().toUtc()),
       _backend =
           backend ??
           (Platform.isAndroid
               ? AndroidMethodChannelLocalEncryptionBackend(
                   deviceSecurityChannel ??
                       const MethodChannel('pos_app/device_security'),
                 )
               : SecureStorageLocalEncryptionBackend(
                   storage ?? const FlutterSecureStorage(),
                 ));

  final LocalEncryptionBackend _backend;
  final DateTime Function() _clock;

  Future<LocalEncryptionKeyState> loadOrCreateState() {
    return _backend.loadOrCreateState(now: _clock().toUtc());
  }

  Future<String> loadOrCreateKey() async {
    final state = await loadOrCreateState();

    return state.active.key;
  }

  Future<LocalEncryptionKeyState> beginRotation({required String reason}) {
    return _backend.beginRotation(reason: reason, now: _clock().toUtc());
  }

  Future<LocalEncryptionKeyState> commitRotation() {
    return _backend.commitRotation();
  }

  Future<LocalEncryptionKeyState> cancelRotation() {
    return _backend.cancelRotation();
  }
}

class SecureStorageLocalEncryptionBackend implements LocalEncryptionBackend {
  SecureStorageLocalEncryptionBackend(this._storage);

  static const _stateStorageKey = 'pos_sqlite_key_state_v2';

  final FlutterSecureStorage _storage;

  @override
  Future<LocalEncryptionKeyState> loadOrCreateState({
    required DateTime now,
  }) async {
    final existing = await _readState();

    if (existing != null) {
      return existing;
    }

    final created = LocalEncryptionKeyState(
      active: LocalEncryptionKeyMaterial(
        key: _generateKey(),
        version: 1,
        createdAt: now.toUtc(),
      ),
    );
    await _writeState(created);

    return created;
  }

  @override
  Future<LocalEncryptionKeyState> beginRotation({
    required String reason,
    required DateTime now,
  }) async {
    final current = await loadOrCreateState(now: now);

    if (current.pending != null) {
      return current;
    }

    final rotated = LocalEncryptionKeyState(
      active: current.active,
      pending: LocalEncryptionKeyMaterial(
        key: _generateKey(),
        version: current.active.version + 1,
        createdAt: now.toUtc(),
        reason: reason,
      ),
    );
    await _writeState(rotated);

    return rotated;
  }

  @override
  Future<LocalEncryptionKeyState> commitRotation() async {
    final current = await _loadExistingOrThrow();
    final pending = current.pending;

    if (pending == null) {
      return current;
    }

    final committed = LocalEncryptionKeyState(active: pending);
    await _writeState(committed);

    return committed;
  }

  @override
  Future<LocalEncryptionKeyState> cancelRotation() async {
    final current = await _loadExistingOrThrow();
    final cancelled = LocalEncryptionKeyState(active: current.active);
    await _writeState(cancelled);

    return cancelled;
  }

  Future<LocalEncryptionKeyState?> _readState() async {
    final raw = await _storage.read(key: _stateStorageKey);

    if (raw == null || raw.isEmpty) {
      return null;
    }

    return LocalEncryptionKeyState.fromJson(
      (jsonDecode(raw) as Map).cast<String, dynamic>(),
    );
  }

  Future<LocalEncryptionKeyState> _loadExistingOrThrow() async {
    final state = await _readState();

    if (state == null) {
      throw StateError('Local encryption key state has not been initialized.');
    }

    return state;
  }

  Future<void> _writeState(LocalEncryptionKeyState state) {
    return _storage.write(
      key: _stateStorageKey,
      value: jsonEncode(state.toJson()),
    );
  }

  String _generateKey() {
    final random = Random.secure();
    final bytes = List<int>.generate(32, (_) => random.nextInt(256));

    return base64UrlEncode(bytes);
  }
}

class AndroidMethodChannelLocalEncryptionBackend
    implements LocalEncryptionBackend {
  AndroidMethodChannelLocalEncryptionBackend(this._channel);

  final MethodChannel _channel;

  @override
  Future<LocalEncryptionKeyState> loadOrCreateState({
    required DateTime now,
  }) async {
    final payload = await _channel.invokeMapMethod<String, dynamic>(
      'loadOrCreateDatabaseKeyState',
    );

    return _parseState(payload, 'Local Android database key state is missing.');
  }

  @override
  Future<LocalEncryptionKeyState> beginRotation({
    required String reason,
    required DateTime now,
  }) async {
    final payload = await _channel.invokeMapMethod<String, dynamic>(
      'beginDatabaseKeyRotation',
      {'reason': reason, 'requested_at_ms': now.toUtc().millisecondsSinceEpoch},
    );

    return _parseState(
      payload,
      'Android database key rotation state could not be created.',
    );
  }

  @override
  Future<LocalEncryptionKeyState> commitRotation() async {
    final payload = await _channel.invokeMapMethod<String, dynamic>(
      'commitDatabaseKeyRotation',
    );

    return _parseState(
      payload,
      'Android database key rotation commit state is missing.',
    );
  }

  @override
  Future<LocalEncryptionKeyState> cancelRotation() async {
    final payload = await _channel.invokeMapMethod<String, dynamic>(
      'cancelDatabaseKeyRotation',
    );

    return _parseState(
      payload,
      'Android database key rotation cancel state is missing.',
    );
  }

  LocalEncryptionKeyState _parseState(
    Map<String, dynamic>? payload,
    String message,
  ) {
    if (payload == null || payload.isEmpty) {
      throw PlatformException(
        code: 'LOCAL_KEY_STATE_UNAVAILABLE',
        message: message,
      );
    }

    return LocalEncryptionKeyState.fromJson(payload);
  }
}
