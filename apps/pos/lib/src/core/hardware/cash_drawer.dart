import 'dart:io';

import 'package:flutter/foundation.dart';

import 'esc_pos_builder.dart';
import 'printer_endpoint_store.dart';

/// Opens the cash drawer. The real implementation pulses the drawer via the
/// receipt printer's kick connector; the debug implementation just records calls.
abstract interface class CashDrawer {
  Future<void> open();
}

/// Debug drawer used in development/tests — records how many times it was opened.
class DebugCashDrawer implements CashDrawer {
  int openCount = 0;

  @override
  Future<void> open() async {
    openCount++;
    debugPrint('[DebugCashDrawer] drawer opened ($openCount)');
  }
}

/// Real drawer — sends the ESC/POS kick pulse to the configured receipt printer's
/// drawer connector over a TCP socket. The drawer hangs off the printer, so we
/// reuse the same `host:port` endpoint map keyed by [drawerPrinterName].
class PrinterKickCashDrawer implements CashDrawer {
  PrinterKickCashDrawer({
    required PrinterEndpointStore endpointStore,
    this.drawerPrinterName = 'receipt-default',
    this.connectTimeout = const Duration(seconds: 5),
  }) : _endpointStore = endpointStore;

  final PrinterEndpointStore _endpointStore;
  final String drawerPrinterName;
  final Duration connectTimeout;

  @override
  Future<void> open() async {
    final endpoint = await _endpointStore.endpointFor(drawerPrinterName);
    final parsed = PrinterEndpointStore.parse(endpoint);
    if (parsed == null) {
      throw StateError(
        'Cash drawer printer "$drawerPrinterName" has no host:port configured.',
      );
    }
    final bytes = EscPosBuilder().drawerKick().build();
    Socket? socket;
    try {
      socket = await Socket.connect(
        parsed.host,
        parsed.port,
        timeout: connectTimeout,
      );
      socket.add(bytes);
      await socket.flush();
    } finally {
      socket?.destroy();
    }
  }
}
