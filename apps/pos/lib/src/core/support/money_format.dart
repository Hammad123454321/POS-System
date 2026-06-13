/// Formats integer minor units (cents) as a human-readable money string using
/// pure integer math — no intl dependency. e.g. formatMinor(1234) => "$12.34".
String formatMinor(int minor, {String currency = 'USD'}) {
  final symbol = _symbolFor(currency);
  final negative = minor < 0;
  final absMinor = minor.abs();
  final whole = absMinor ~/ 100;
  final cents = absMinor % 100;
  final centsStr = cents.toString().padLeft(2, '0');
  final wholeStr = _groupThousands(whole);
  final sign = negative ? '-' : '';

  return '$sign$symbol$wholeStr.$centsStr';
}

String _groupThousands(int value) {
  final digits = value.toString();
  final buffer = StringBuffer();
  final firstGroup = digits.length % 3;

  for (var i = 0; i < digits.length; i++) {
    if (i != 0 && (i - firstGroup) % 3 == 0 && i >= firstGroup) {
      buffer.write(',');
    }
    buffer.write(digits[i]);
  }

  return buffer.toString();
}

String _symbolFor(String currency) {
  switch (currency.toUpperCase()) {
    case 'USD':
    case 'CAD':
    case 'AUD':
      return r'$';
    case 'EUR':
      return '€';
    case 'GBP':
      return '£';
    default:
      return '';
  }
}
