import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/core/hardware/barcode_scanner.dart';
import 'package:pos_app/src/core/hardware/cash_drawer.dart';
import 'package:pos_app/src/core/hardware/esc_pos_builder.dart';

void main() {
  group('EscPosBuilder', () {
    test('init emits ESC @', () {
      final bytes = EscPosBuilder().init().build();
      expect(bytes, [0x1B, 0x40]);
    });

    test('text appends Latin-1 bytes + line feed', () {
      final bytes = EscPosBuilder().text('Hi').build();
      expect(bytes, [0x48, 0x69, 0x0A]);
    });

    test('cut emits GS V 0', () {
      final bytes = EscPosBuilder().cut().build();
      expect(bytes, [0x1D, 0x56, 0x00]);
    });

    test('drawerKick emits the kick sequence', () {
      final bytes = EscPosBuilder().drawerKick().build();
      expect(bytes, [0x1B, 0x70, 0x00, 0x19, 0xFA]);
    });

    test('chains a full receipt', () {
      final bytes = EscPosBuilder().init().text('Store').feed(2).cut().build();
      expect(bytes.first, 0x1B);
      expect(bytes.last, 0x00);
    });
  });

  group('DebugCashDrawer', () {
    test('counts opens', () async {
      final drawer = DebugCashDrawer();
      await drawer.open();
      await drawer.open();
      expect(drawer.openCount, 2);
    });
  });

  group('HidScanAccumulator', () {
    test('emits a code on Enter when fast enough and long enough', () {
      final acc = HidScanAccumulator();
      var t = DateTime(2026);
      for (final c in '12345'.split('')) {
        acc.onChar(c, t);
        t = t.add(const Duration(milliseconds: 10));
      }
      expect(acc.onEnter(), '12345');
    });

    test('resets on a slow inter-key gap (human typing)', () {
      final acc = HidScanAccumulator();
      var t = DateTime(2026);
      acc.onChar('1', t);
      t = t.add(const Duration(milliseconds: 500)); // slow
      acc.onChar('2', t);
      t = t.add(const Duration(milliseconds: 10));
      acc.onChar('3', t);
      // Buffer was reset by the slow gap, so only "23" remains -> below minLength.
      expect(acc.onEnter(), isNull);
    });

    test('rejects short codes below minLength', () {
      final acc = HidScanAccumulator(minLength: 4);
      acc.onChar('1', DateTime(2026));
      expect(acc.onEnter(), isNull);
    });
  });

  group('DebugBarcodeScanner', () {
    test('streams emitted codes', () async {
      final scanner = DebugBarcodeScanner();
      final future = scanner.scans.first;
      scanner.emit('ABC123');
      expect(await future, 'ABC123');
      scanner.dispose();
    });
  });
}
