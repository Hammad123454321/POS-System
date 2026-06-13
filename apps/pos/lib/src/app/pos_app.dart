import 'package:flutter/material.dart';

import '../bootstrap/app_bootstrapper.dart';
import '../features/home/pos_home_screen.dart';
import 'pos_theme.dart';

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
      theme: PosTheme.light(),
      home: PosHomeScreen(controller: widget.bootstrap.controller),
    );
  }
}
