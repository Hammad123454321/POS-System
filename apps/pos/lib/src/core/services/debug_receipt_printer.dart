import 'package:flutter/foundation.dart';

import 'receipt_printer.dart';

class DebugReceiptPrinter implements ReceiptPrinter {
  @override
  Future<void> printReceipt({
    required String receiptId,
    required String routeKey,
    required List<String> printerNames,
    required Map<String, dynamic> payload,
  }) async {
    debugPrint(
      'Printing receipt $receiptId via $routeKey on ${printerNames.join(', ')}: '
      '${payload['order_number'] ?? ''}',
    );
  }
}
