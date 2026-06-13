import 'dart:io';
import 'dart:typed_data';

import '../services/receipt_printer.dart';
import 'esc_pos_builder.dart';
import 'printer_endpoint_store.dart';

/// Renders a receipt payload to ESC/POS bytes and streams them to a networked
/// thermal printer over a raw TCP socket (RAW/JetDirect, port 9100 by default).
///
/// The byte rendering ([renderReceipt]) is a pure static function so it can be
/// unit-tested without any I/O. The socket send keeps the existing spool
/// semantics: a throw leaves the job pending in the 7-day print queue so the
/// controller retries it later — no controller change needed.
class NetworkEscPosReceiptPrinter implements ReceiptPrinter {
  NetworkEscPosReceiptPrinter({
    required PrinterEndpointStore endpointStore,
    this.connectTimeout = const Duration(seconds: 5),
    this.columns = 42,
  }) : _endpointStore = endpointStore;

  final PrinterEndpointStore _endpointStore;
  final Duration connectTimeout;
  final int columns;

  @override
  Future<void> printReceipt({
    required String receiptId,
    required String routeKey,
    required List<String> printerNames,
    required Map<String, dynamic> payload,
  }) async {
    if (printerNames.isEmpty) {
      throw StateError(
        'No printer assigned to route "$routeKey" (receipt $receiptId).',
      );
    }

    final bytes = renderReceipt(payload, columns: columns);

    // Send to every printer on the route. A single failure throws so the whole
    // job stays pending and is retried (idempotent at the spool level).
    for (final name in printerNames) {
      final endpoint = await _endpointStore.endpointFor(name);
      final parsed = PrinterEndpointStore.parse(endpoint);
      if (parsed == null) {
        throw StateError(
          'Printer "$name" has no host:port configured on this device.',
        );
      }
      await _send(parsed.host, parsed.port, bytes);
    }
  }

  Future<void> _send(String host, int port, Uint8List bytes) async {
    Socket? socket;
    try {
      socket = await Socket.connect(host, port, timeout: connectTimeout);
      socket.add(bytes);
      await socket.flush();
    } finally {
      socket?.destroy();
    }
  }

  /// Pure: builds the ESC/POS byte stream for a receipt payload. Tolerant of
  /// missing keys so partial/legacy payloads still print something useful.
  static Uint8List renderReceipt(
    Map<String, dynamic> payload, {
    int columns = 42,
  }) {
    final b = EscPosBuilder().init();

    final storeName = (payload['store_name'] ?? payload['merchant_name'] ?? '')
        .toString();
    if (storeName.isNotEmpty) b.text(_center(storeName, columns));

    final orderNumber = (payload['order_number'] ?? '').toString();
    final receiptNumber = (payload['receipt_number'] ?? '').toString();
    if (orderNumber.isNotEmpty) b.text('Order: $orderNumber');
    if (receiptNumber.isNotEmpty) b.text('Receipt: $receiptNumber');
    b.text(_rule(columns));

    final lines = payload['lines'];
    if (lines is List) {
      for (final raw in lines) {
        if (raw is! Map) continue;
        final qty = raw['quantity'] ?? 1;
        final name = (raw['name'] ?? raw['catalog_item_name'] ?? 'Item')
            .toString();
        final amount = _asMinor(raw['amount_minor'] ?? raw['total_minor']);
        b.text(_lineItem('$qty x $name', _money(amount), columns));
      }
      b.text(_rule(columns));
    }

    final subtotal = _asMinor(payload['subtotal_minor']);
    final tax = _asMinor(payload['tax_minor']);
    final discount = _asMinor(payload['discount_minor']);
    final tip = _asMinor(payload['tip_minor']);
    final total = _asMinor(payload['total_minor']);
    final change = _asMinor(payload['change_minor']);

    if (subtotal != null) b.text(_lineItem('Subtotal', _money(subtotal), columns));
    if (discount != null && discount != 0) {
      b.text(_lineItem('Discount', '-${_money(discount)}', columns));
    }
    if (tax != null) b.text(_lineItem('Tax', _money(tax), columns));
    if (tip != null && tip != 0) {
      b.text(_lineItem('Tip', _money(tip), columns));
    }
    if (total != null) b.text(_lineItem('TOTAL', _money(total), columns));
    if (change != null && change != 0) {
      b.text(_lineItem('Change', _money(change), columns));
    }

    final payments = payload['payments'];
    if (payments is List && payments.isNotEmpty) {
      b.text(_rule(columns));
      for (final raw in payments) {
        if (raw is! Map) continue;
        final method = (raw['method'] ?? 'payment').toString();
        final amount = _asMinor(raw['amount_minor']);
        b.text(_lineItem(_titleCase(method), _money(amount), columns));
      }
    }

    b.feed(1).text(_center('Thank you!', columns)).feed(3).cut();
    return b.build();
  }

  static int? _asMinor(Object? v) => v is int ? v : (v is num ? v.toInt() : null);

  static String _money(int? minor) {
    if (minor == null) return '';
    final negative = minor < 0;
    final abs = minor.abs();
    final major = abs ~/ 100;
    final cents = (abs % 100).toString().padLeft(2, '0');
    return '${negative ? '-' : ''}\$$major.$cents';
  }

  static String _rule(int columns) => '-' * columns;

  static String _center(String s, int columns) {
    if (s.length >= columns) return s.substring(0, columns);
    final pad = (columns - s.length) ~/ 2;
    return '${' ' * pad}$s';
  }

  /// Left label + right-aligned value on one [columns]-wide line.
  static String _lineItem(String left, String right, int columns) {
    final maxLeft = columns - right.length - 1;
    final l = left.length > maxLeft && maxLeft > 0
        ? left.substring(0, maxLeft)
        : left;
    final gap = columns - l.length - right.length;
    return '$l${' ' * (gap < 1 ? 1 : gap)}$right';
  }

  static String _titleCase(String s) =>
      s.isEmpty ? s : s[0].toUpperCase() + s.substring(1);
}
