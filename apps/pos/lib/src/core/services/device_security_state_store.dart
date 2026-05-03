import 'package:flutter_secure_storage/flutter_secure_storage.dart';

abstract interface class DeviceSecurityStateStore {
  Future<bool> requiresReauth();
  Future<void> setRequiresReauth(bool value);
}

class SecureDeviceSecurityStateStore implements DeviceSecurityStateStore {
  SecureDeviceSecurityStateStore([FlutterSecureStorage? storage])
    : _storage = storage ?? const FlutterSecureStorage();

  static const _keyName = 'pos_device_requires_reauth_v1';

  final FlutterSecureStorage _storage;

  @override
  Future<bool> requiresReauth() async {
    final raw = await _storage.read(key: _keyName);

    return raw == 'true';
  }

  @override
  Future<void> setRequiresReauth(bool value) async {
    if (value) {
      await _storage.write(key: _keyName, value: 'true');
      return;
    }

    await _storage.delete(key: _keyName);
  }
}
