import 'package:flutter/material.dart';

import '../bootstrap/app_bootstrapper.dart';
import '../features/home/pos_home_screen.dart';

class PosApp extends StatefulWidget {
  const PosApp({required this.bootstrap, super.key});

  final AppBootstrap bootstrap;

  @override
  State<PosApp> createState() => _PosAppState();
}

class _PosAppState extends State<PosApp> {
  @override
  void dispose() {
    widget.bootstrap.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'POS Device Shell',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF1F3A2E),
          brightness: Brightness.light,
          surface: const Color(0xFFF8F4EC),
        ),
        scaffoldBackgroundColor: const Color(0xFFF4F0E8),
        useMaterial3: true,
      ),
      home: PosHomeScreen(controller: widget.bootstrap.controller),
    );
  }
}
