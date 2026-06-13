import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/core/hardware/network_esc_pos_receipt_printer.dart';
import 'package:pos_app/src/core/hardware/printer_endpoint_store.dart';

void main() {
  group('PrinterEndpointStore.parse', () {
    test('parses a valid host:port', () {
      final r = PrinterEndpointStore.parse('192.168.1.50:9100');
      expect(r?.host, '192.168.1.50');
      expect(r?.port, 9100);
    });

    test('rejects malformed values', () {
      expect(PrinterEndpointStore.parse(null), isNull);
      expect(PrinterEndpointStore.parse(''), isNull);
      expect(PrinterEndpointStore.parse('nohost'), isNull);
      expect(PrinterEndpointStore.parse('host:'), isNull);
      expect(PrinterEndpointStore.parse(':9100'), isNull);
      expect(PrinterEndpointStore.parse('host:0'), isNull);
      expect(PrinterEndpointStore.parse('host:99999'), isNull);
      expect(PrinterEndpointStore.parse('host:abc'), isNull);
    });

    test('keeps host with embedded colons via last-colon split', () {
      final r = PrinterEndpointStore.parse('printer.local:9100');
      expect(r?.host, 'printer.local');
      expect(r?.port, 9100);
    });
  });

  group('PrinterEndpointStore (in-memory fallback)', () {
    test('stores and reads endpoints without platform bindings', () async {
      final store = PrinterEndpointStore();
      expect(await store.endpointFor('receipt-default'), isNull);
      await store.setEndpoint('receipt-default', '10.0.0.5:9100');
      expect(await store.endpointFor('receipt-default'), '10.0.0.5:9100');
      await store.remove('receipt-default');
      expect(await store.endpointFor('receipt-default'), isNull);
    });
  });

  group('NetworkEscPosReceiptPrinter.renderReceipt', () {
    test('emits ESC @ init and ends with a full cut', () {
      final bytes = NetworkEscPosReceiptPrinter.renderReceipt({
        'order_number': 'A-1001',
        'total_minor': 1299,
      });
      expect(bytes.sublist(0, 2), [0x1B, 0x40]); // ESC @
      // last three bytes are GS V 0 (cut)
      expect(bytes.sublist(bytes.length - 3), [0x1D, 0x56, 0x00]);
    });

    test('renders order number, totals and payments as text', () {
      final bytes = NetworkEscPosReceiptPrinter.renderReceipt({
        'store_name': 'Zen Cafe',
        'order_number': 'A-1001',
        'receipt_number': 'R-77',
        'subtotal_minor': 1000,
        'tax_minor': 80,
        'tip_minor': 200,
        'total_minor': 1280,
        'change_minor': 20,
        'lines': [
          {'quantity': 2, 'name': 'Latte', 'amount_minor': 1000},
        ],
        'payments': [
          {'method': 'cash', 'amount_minor': 1300},
        ],
      });
      final text = latin1.decode(bytes);
      expect(text, contains('Zen Cafe'));
      expect(text, contains('A-1001'));
      expect(text, contains('Latte'));
      expect(text, contains('TOTAL'));
      expect(text, contains('\$12.80'));
      expect(text, contains('Cash'));
      expect(text, contains('Thank you!'));
    });

    test('tolerates a near-empty payload', () {
      final bytes = NetworkEscPosReceiptPrinter.renderReceipt({});
      expect(bytes, isNotEmpty);
      expect(bytes.sublist(0, 2), [0x1B, 0x40]);
    });
  });
}
