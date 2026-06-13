import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/core/support/money_format.dart';

void main() {
  group('formatMinor', () {
    test('formats simple amounts with USD symbol', () {
      expect(formatMinor(1234), r'$12.34');
      expect(formatMinor(5), r'$0.05');
      expect(formatMinor(100), r'$1.00');
    });

    test('groups thousands', () {
      expect(formatMinor(123456789), r'$1,234,567.89');
      expect(formatMinor(100000), r'$1,000.00');
    });

    test('handles negative amounts', () {
      expect(formatMinor(-2500), r'-$25.00');
    });

    test('uses currency-specific symbols', () {
      expect(formatMinor(999, currency: 'EUR'), '€9.99');
      expect(formatMinor(999, currency: 'GBP'), '£9.99');
    });

    test('omits symbol for unknown currency', () {
      expect(formatMinor(999, currency: 'JPY'), '9.99');
    });
  });
}
