import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// Emits scanned barcodes. The real implementation listens to HID keyboard
/// wedge input; the debug implementation lets tests/dev push codes manually.
abstract interface class BarcodeScanner {
  Stream<String> get scans;
  void dispose();
}

/// Real scanner — most retail barcode scanners present as HID keyboards that
/// type the code very fast and press Enter. This listens on the global
/// [HardwareKeyboard] handler, accumulates rapid keystrokes via
/// [HidScanAccumulator], and emits on Enter. It stays silent while a real text
/// field has focus so cashiers can still type into forms.
class HidKeyboardBarcodeScanner implements BarcodeScanner {
  HidKeyboardBarcodeScanner({HidScanAccumulator? accumulator})
    : _acc = accumulator ?? HidScanAccumulator() {
    HardwareKeyboard.instance.addHandler(_onKey);
  }

  final HidScanAccumulator _acc;
  final _controller = StreamController<String>.broadcast();

  @override
  Stream<String> get scans => _controller.stream;

  bool _onKey(KeyEvent event) {
    if (event is! KeyDownEvent && event is! KeyRepeatEvent) return false;

    // Don't intercept while the user is typing into a focused text field.
    final focus = FocusManager.instance.primaryFocus;
    if (focus != null && focus.context?.widget is EditableText) return false;

    final now = DateTime.fromMillisecondsSinceEpoch(event.timeStamp.inMilliseconds);

    if (event.logicalKey == LogicalKeyboardKey.enter ||
        event.logicalKey == LogicalKeyboardKey.numpadEnter) {
      final code = _acc.onEnter();
      if (code != null) {
        _controller.add(code);
        return true; // consume the Enter that completed a scan
      }
      return false;
    }

    final char = event.character;
    if (char != null && char.length == 1 && !_isControl(char)) {
      _acc.onChar(char, now);
    }
    return false;
  }

  bool _isControl(String c) => c.codeUnitAt(0) < 0x20;

  @override
  void dispose() {
    HardwareKeyboard.instance.removeHandler(_onKey);
    _controller.close();
  }
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
