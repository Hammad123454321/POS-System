package com.posplatform.pos_app

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "pos_app/device_security",
        ).setMethodCallHandler(DeviceSecurityChannel(applicationContext))

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "pos_app/pax_terminal",
        ).setMethodCallHandler(PaxTerminalChannel(applicationContext))
    }
}
