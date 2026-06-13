import 'package:flutter/material.dart';

/// Centralized colors + theme for the POS app. Extracted from the original
/// inline literals so new screens share one source of truth. The legacy console
/// screen keeps its own literals untouched.
class PosColors {
  static const Color forest = Color(0xFF1F3A2E);
  static const Color cream = Color(0xFFF4F0E8);
  static const Color surface = Color(0xFFF8F4EC);

  // Table status colors.
  static const Color tableAvailable = Color(0xFF6FA287);
  static const Color tableClaimedByMe = Color(0xFF1F3A2E);
  static const Color tableClaimedOther = Color(0xFFC8962B);
  static const Color tableOccupied = Color(0xFF4A5D52);

  // Appointment status colors.
  static const Color apptBooked = Color(0xFF2F6B47);
  static const Color apptCheckedIn = Color(0xFF1F8A70);
  static const Color apptCompleted = Color(0xFF6B7B73);
  static const Color apptCancelled = Color(0xFFB54A3A);
}

class PosTheme {
  static ThemeData light() {
    return ThemeData(
      colorScheme: ColorScheme.fromSeed(
        seedColor: PosColors.forest,
        brightness: Brightness.light,
        surface: PosColors.surface,
      ),
      scaffoldBackgroundColor: PosColors.cream,
      useMaterial3: true,
    );
  }
}
