import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Maps a logical printer name (as carried in print routes) to a physical
/// `host:port` socket address. Print routes carry no network address, so the
/// device keeps this local map (set once per station). Persisted in secure
/// storage as a single JSON blob; an in-memory cache keeps reads cheap and lets
/// it work in tests without platform bindings.
class PrinterEndpointStore {
  PrinterEndpointStore({FlutterSecureStorage? storage})
    : _storage = storage ?? const FlutterSecureStorage();

  static const _storageKey = 'printer_endpoints';

  final FlutterSecureStorage _storage;
  Map<String, String>? _cache;

  Future<Map<String, String>> _load() async {
    if (_cache != null) return _cache!;
    try {
      final raw = await _storage.read(key: _storageKey);
      if (raw == null || raw.isEmpty) {
        _cache = {};
      } else {
        final decoded = jsonDecode(raw) as Map<String, dynamic>;
        _cache = decoded.map((k, v) => MapEntry(k, v.toString()));
      }
    } catch (_) {
      // No platform bindings (pure unit test) — stay in-memory.
      _cache = {};
    }
    return _cache!;
  }

  Future<void> _persist() async {
    try {
      await _storage.write(key: _storageKey, value: jsonEncode(_cache ?? {}));
    } catch (_) {
      // In-memory only; nothing to persist.
    }
  }

  Future<void> setEndpoint(String printerName, String hostPort) async {
    final map = await _load();
    map[printerName] = hostPort;
    await _persist();
  }

  Future<void> remove(String printerName) async {
    final map = await _load();
    map.remove(printerName);
    await _persist();
  }

  Future<Map<String, String>> all() async => Map.unmodifiable(await _load());

  /// Returns `host:port` for [printerName], or null if unmapped.
  Future<String?> endpointFor(String printerName) async {
    final map = await _load();
    return map[printerName];
  }

  /// Parses a stored `host:port` string into its parts. Returns null if the
  /// value is malformed.
  static ({String host, int port})? parse(String? hostPort) {
    if (hostPort == null) return null;
    final idx = hostPort.lastIndexOf(':');
    if (idx <= 0 || idx == hostPort.length - 1) return null;
    final host = hostPort.substring(0, idx).trim();
    final port = int.tryParse(hostPort.substring(idx + 1).trim());
    if (host.isEmpty || port == null || port <= 0 || port > 65535) return null;
    return (host: host, port: port);
  }
}
