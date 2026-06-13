import 'dart:async';

/// Emits scanned barcodes. The real implementation listens to HID keyboard
/// wedge input; the debug implementation lets tests/dev push codes manually.
abstract interface class BarcodeScanner {
  Stream<String> get scans;
  void dispose();
}

/// Debug scanner — push codes via [emit].
class DebugBarcodeScanner implements BarcodeScanner {
  final _controller = StreamController<String>.broadcast();

  @override
  Stream<String> get scans => _controller.stream;

  void emit(String code) => _controller.add(code);

  @override
  void dispose() => _controller.close();
}

/// Accumulates rapid keystrokes from a HID keyboard-wedge scanner and emits a
/// completed scan. Pure buffer logic (no Flutter binding) so it is unit-testable;
/// a thin widget feeds it key events and inter-key timing.
class HidScanAccumulator {
  HidScanAccumulator({
    this.minLength = 4,
    this.maxInterKeyGap = const Duration(milliseconds: 50),
  });

  final int minLength;
  final Duration maxInterKeyGap;

  final StringBuffer _buffer = StringBuffer();
  DateTime? _lastKeyAt;

  /// Feed a character with the timestamp it arrived. A gap larger than
  /// [maxInterKeyGap] resets the buffer (human typing, not a scanner burst).
  void onChar(String char, DateTime at) {
    if (_lastKeyAt != null && at.difference(_lastKeyAt!) > maxInterKeyGap) {
      _buffer.clear();
    }
    _buffer.write(char);
    _lastKeyAt = at;
  }

  /// Called on Enter. Returns the buffered code if it meets [minLength], else null.
  String? onEnter() {
    final code = _buffer.toString();
    _buffer.clear();
    _lastKeyAt = null;
    return code.length >= minLength ? code : null;
  }
}
