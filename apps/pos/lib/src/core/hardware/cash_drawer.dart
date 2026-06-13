import 'package:flutter/foundation.dart';

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
