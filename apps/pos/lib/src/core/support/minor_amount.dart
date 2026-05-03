final class MinorAmount {
  static const int basisPointsScale = 10000;

  static int calculateTax({
    required int netMinor,
    required int rateBasisPoints,
    required bool inclusive,
  }) {
    if (netMinor <= 0 || rateBasisPoints <= 0) {
      return 0;
    }

    if (inclusive) {
      final exclusiveBase = _divideAndRoundHalfUp(
        dividend: netMinor * basisPointsScale,
        divisor: basisPointsScale + rateBasisPoints,
      );

      return netMinor - exclusiveBase;
    }

    return _divideAndRoundHalfUp(
      dividend: netMinor * rateBasisPoints,
      divisor: basisPointsScale,
    );
  }

  static int percentageOf({
    required int amountMinor,
    required int basisPoints,
  }) {
    if (amountMinor <= 0 || basisPoints <= 0) {
      return 0;
    }

    return (amountMinor * basisPoints) ~/ basisPointsScale;
  }

  static List<int> allocateAcross(List<int> baseAmounts, int allocationMinor) {
    if (allocationMinor <= 0 || baseAmounts.isEmpty) {
      return List<int>.filled(baseAmounts.length, 0, growable: false);
    }

    final baseTotalMinor = baseAmounts.fold<int>(0, (sum, item) => sum + item);

    if (baseTotalMinor <= 0) {
      return List<int>.filled(baseAmounts.length, 0, growable: false);
    }

    final allocations = List<int>.filled(
      baseAmounts.length,
      0,
      growable: false,
    );
    var allocatedMinor = 0;

    for (var index = 0; index < baseAmounts.length; index += 1) {
      if (index == baseAmounts.length - 1) {
        allocations[index] = (allocationMinor - allocatedMinor).clamp(
          0,
          baseAmounts[index],
        );
        continue;
      }

      final allocation =
          (allocationMinor * baseAmounts[index]) ~/ baseTotalMinor;
      allocations[index] = allocation.clamp(0, baseAmounts[index]);
      allocatedMinor += allocations[index];
    }

    return allocations;
  }

  static int _divideAndRoundHalfUp({
    required int dividend,
    required int divisor,
  }) {
    if (divisor == 0) {
      throw ArgumentError.value(divisor, 'divisor', 'Must not be zero.');
    }

    final sign = dividend.sign * divisor.sign;
    final absoluteDividend = dividend.abs();
    final absoluteDivisor = divisor.abs();
    var quotient = absoluteDividend ~/ absoluteDivisor;
    final remainder = absoluteDividend % absoluteDivisor;

    if (remainder * 2 >= absoluteDivisor) {
      quotient += 1;
    }

    return sign < 0 ? -quotient : quotient;
  }
}
