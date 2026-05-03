import 'package:flutter/widgets.dart';

import 'src/app/pos_app.dart';
import 'src/bootstrap/app_bootstrapper.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final bootstrap = await AppBootstrapper.bootstrap();

  runApp(PosApp(bootstrap: bootstrap));
}
