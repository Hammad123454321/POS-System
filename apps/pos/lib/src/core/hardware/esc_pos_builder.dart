import 'dart:convert';
import 'dart:typed_data';

/// Builds an ESC/POS byte stream for thermal receipt printers. Pure (no I/O) so
/// it is fully unit-testable; a transport (network/USB) sends the bytes.
class EscPosBuilder {
  final List<int> _buffer = [];

  /// ESC @ — initialize printer.
  EscPosBuilder init() {
    _buffer.addAll([0x1B, 0x40]);
    return this;
  }

  /// Append a line of text (ASCII/Latin-1) followed by a line feed.
  EscPosBuilder text(String value) {
    _buffer.addAll(latin1.encode(value));
    _buffer.add(0x0A);
    return this;
  }

  /// Feed n blank lines.
  EscPosBuilder feed([int lines = 1]) {
    for (var i = 0; i < lines; i++) {
      _buffer.add(0x0A);
    }
    return this;
  }

  /// GS V 0 — full cut.
  EscPosBuilder cut() {
    _buffer.addAll([0x1D, 0x56, 0x00]);
    return this;
  }

  /// ESC p 0 — pulse the cash-drawer kick connector.
  EscPosBuilder drawerKick() {
    _buffer.addAll([0x1B, 0x70, 0x00, 0x19, 0xFA]);
    return this;
  }

  Uint8List build() => Uint8List.fromList(_buffer);
}
