import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/core/support/minor_amount.dart';

void main() {
  test(
    'calculates exclusive tax using minor-unit rounding without doubles',
    () {
      expect(
        MinorAmount.calculateTax(
          netMinor: 1005,
          rateBasisPoints: 1000,
          inclusive: false,
        ),
        101,
      );
    },
  );

  test(
    'calculates inclusive tax using minor-unit rounding without doubles',
    () {
      expect(
        MinorAmount.calculateTax(
          netMinor: 1075,
          rateBasisPoints: 750,
          inclusive: true,
        ),
        75,
      );
    },
  );

  test(
    'allocates discounts by minor units and leaves the remainder on the last line',
    () {
      expect(MinorAmount.allocateAcross(<int>[333, 333, 334], 100), <int>[
        33,
        33,
        34,
      ]);
    },
  );

  test('calculates percentage discounts with minor-unit floor rounding', () {
    expect(MinorAmount.percentageOf(amountMinor: 999, basisPoints: 333), 33);
  });
}
