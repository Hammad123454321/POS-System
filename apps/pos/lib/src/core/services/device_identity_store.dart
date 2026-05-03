import 'dart:convert';
import 'dart:math';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../models/pos_models.dart';

class DeviceIdentityStore {
  DeviceIdentityStore([
    FlutterSecureStorage? storage,
    MethodChannel? deviceSecurityChannel,
  ]) : _storage = storage ?? const FlutterSecureStorage(),
       _deviceSecurityChannel =
           deviceSecurityChannel ??
           const MethodChannel('pos_app/device_security');

  static const _publicKeyName = 'pos_device_public_key_v1';
  static const _fingerprintName = 'pos_device_fingerprint_v1';
  static const _attestationName = 'pos_device_attestation_v1';

  final FlutterSecureStorage _storage;
  final MethodChannel _deviceSecurityChannel;

  Future<DeviceIdentity> loadOrCreate() async {
    if (Platform.isAndroid) {
      return _loadOrCreateAndroidIdentity();
    }

    return _loadOrCreateLegacyIdentity();
  }

  Future<DeviceIdentity> _loadOrCreateAndroidIdentity() async {
    final payload = await _deviceSecurityChannel
        .invokeMapMethod<String, dynamic>('loadOrCreateEnrollmentIdentity');

    if (payload == null) {
      throw PlatformException(
        code: 'DEVICE_IDENTITY_UNAVAILABLE',
        message: 'Android enrollment identity could not be resolved.',
      );
    }

    final publicKey = payload['public_key'] as String?;
    final deviceFingerprint = payload['device_fingerprint'] as String?;
    final attestation = (payload['attestation'] as Map?)
        ?.cast<String, dynamic>();

    if (publicKey == null ||
        publicKey.isEmpty ||
        deviceFingerprint == null ||
        deviceFingerprint.isEmpty ||
        attestation == null ||
        attestation.isEmpty) {
      throw PlatformException(
        code: 'DEVICE_IDENTITY_INVALID',
        message: 'Android enrollment identity payload is incomplete.',
      );
    }

    return DeviceIdentity(
      publicKey: publicKey,
      deviceFingerprint: deviceFingerprint,
      attestation: attestation,
    );
  }

  Future<DeviceIdentity> _loadOrCreateLegacyIdentity() async {
    final existingPublicKey = await _storage.read(key: _publicKeyName);
    final existingFingerprint = await _storage.read(key: _fingerprintName);
    final existingAttestation = await _storage.read(key: _attestationName);

    if (existingPublicKey != null &&
        existingPublicKey.isNotEmpty &&
        existingFingerprint != null &&
        existingFingerprint.isNotEmpty &&
        existingAttestation != null &&
        existingAttestation.isNotEmpty) {
      return DeviceIdentity(
        publicKey: existingPublicKey,
        deviceFingerprint: existingFingerprint,
        attestation: (jsonDecode(existingAttestation) as Map)
            .cast<String, dynamic>(),
      );
    }

    final platform = Platform.operatingSystem;
    final identity = DeviceIdentity(
      publicKey: _randomToken(48),
      deviceFingerprint: _randomToken(24),
      attestation: <String, dynamic>{
        'provider': 'legacy_secure_storage',
        'platform': platform,
      },
    );

    await _storage.write(key: _publicKeyName, value: identity.publicKey);
    await _storage.write(
      key: _fingerprintName,
      value: identity.deviceFingerprint,
    );
    await _storage.write(
      key: _attestationName,
      value: jsonEncode(identity.attestation),
    );

    return identity;
  }

  String _randomToken(int byteLength) {
    final random = Random.secure();
    final bytes = List<int>.generate(byteLength, (_) => random.nextInt(256));

    return base64UrlEncode(bytes);
  }
}
