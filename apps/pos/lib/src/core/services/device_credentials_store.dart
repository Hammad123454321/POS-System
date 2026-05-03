import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../models/pos_models.dart';

abstract interface class DeviceCredentialsStore {
  Future<DeviceCredentials?> load();
  Future<void> save(DeviceCredentials credentials);
  Future<void> clear();
}

class SecureDeviceCredentialsStore implements DeviceCredentialsStore {
  SecureDeviceCredentialsStore([FlutterSecureStorage? storage])
    : _storage = storage ?? const FlutterSecureStorage();

  static const _keyName = 'pos_device_credentials_v1';

  final FlutterSecureStorage _storage;

  @override
  Future<DeviceCredentials?> load() async {
    final raw = await _storage.read(key: _keyName);

    if (raw == null || raw.isEmpty) {
      return null;
    }

    return DeviceCredentials.fromJson(jsonDecode(raw) as Map<String, dynamic>);
  }

  @override
  Future<void> save(DeviceCredentials credentials) async {
    await _storage.write(
      key: _keyName,
      value: jsonEncode(credentials.toJson()),
    );
  }

  @override
  Future<void> clear() async {
    await _storage.delete(key: _keyName);
  }
}
