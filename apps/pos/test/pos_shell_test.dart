import 'package:drift/drift.dart' show Value;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/app/pos_theme.dart';
import 'package:pos_app/src/core/models/pos_models.dart';
import 'package:pos_app/src/core/services/device_credentials_store.dart';
import 'package:pos_app/src/core/services/device_security_state_store.dart';
import 'package:pos_app/src/core/services/local_data_protection_service.dart';
import 'package:pos_app/src/core/services/pax_terminal_gateway.dart';
import 'package:pos_app/src/core/services/receipt_printer.dart';
import 'package:pos_app/src/data/local/pos_database.dart';
import 'package:pos_app/src/data/remote/pos_gateway.dart';
import 'package:pos_app/src/data/repositories/bootstrap_cache_repository.dart';
import 'package:pos_app/src/data/repositories/receipt_print_queue_repository.dart';
import 'package:pos_app/src/data/repositories/sync_outbox_repository.dart';
import 'package:pos_app/src/features/home/pos_home_controller.dart';
import 'package:pos_app/src/navigation/pos_shell.dart';

Future<PosHomeController> _buildController(PosDatabase database) async {
  await database.into(database.deviceBootstrapCaches).insert(
        DeviceBootstrapCachesCompanion.insert(
          id: 'b1',
          apiBaseUrl: const Value('https://example.test'),
          deviceId: const Value('device-1'),
          merchantId: const Value('merchant-1'),
          storeId: const Value('store-1'),
          payloadJson: '''
{
  "bootstrap": {"merchant": {"id": "merchant-1"}, "store": {"id": "store-1"}, "device": {"id": "device-1"}, "register": {"active_session": null}},
  "config": {"items": [], "tax_rules": [], "discount_rules": [], "membership_plans": [], "payment_capabilities": {"default_provider": "fiserv_bluepay", "supported_tenders": ["cash"], "terminal_capabilities": {}}, "print_routes": []},
  "tables": {"tables": []},
  "business_day_summary": {"business_date": "2026-04-24", "open_orders_count": 0, "paid_orders_count": 0, "gross_sales_minor": 0, "cash_sales_minor": 0, "open_register_sessions_count": 0, "open_exception_cases_count": 0},
  "exceptions": {"count": 0, "cases": []},
  "deltas": {"cursor": "2026-04-24T12:00:00Z"}
}
''',
          syncedAt: DateTime(2026, 4, 24, 12),
        ),
      );

  final controller = PosHomeController(
    bootstrapCacheRepository: BootstrapCacheRepository(database),
    syncOutboxRepository: SyncOutboxRepository(database),
    receiptPrintQueueRepository: ReceiptPrintQueueRepository(database),
    credentialsStore: _Creds(),
    securityStateStore: _Sec(),
    gateway: _Gw(),
    terminalGateway: _Term(),
    receiptPrinter: _Printer(),
    dataProtectionService: _Data(),
  );
  await controller.load();
  return controller;
}

void main() {
  testWidgets('renders a bottom navigation bar on narrow layouts', (tester) async {
    tester.view.physicalSize = const Size(420, 900);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    final database = PosDatabase.memory();
    final controller = await _buildController(database);

    await tester.pumpWidget(
      MaterialApp(theme: PosTheme.light(), home: PosShell(controller: controller)),
    );
    await tester.pumpAndSettle();

    expect(find.byType(NavigationBar), findsOneWidget);
    expect(find.byType(NavigationRail), findsNothing);

    controller.dispose();
    await database.close();
  });

  testWidgets('renders a navigation rail on wide layouts', (tester) async {
    tester.view.physicalSize = const Size(1280, 800);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    final database = PosDatabase.memory();
    final controller = await _buildController(database);

    await tester.pumpWidget(
      MaterialApp(theme: PosTheme.light(), home: PosShell(controller: controller)),
    );
    await tester.pumpAndSettle();

    expect(find.byType(NavigationRail), findsOneWidget);
    expect(find.byType(NavigationBar), findsNothing);

    controller.dispose();
    await database.close();
  });
}

class _Creds implements DeviceCredentialsStore {
  @override
  Future<DeviceCredentials?> load() async => DeviceCredentials(
        apiBaseUrl: 'https://example.test',
        deviceId: 'device-1',
        accessToken: 'a',
        accessTokenExpiresAt: DateTime.utc(2099),
        refreshToken: 'r',
        refreshTokenExpiresAt: DateTime.utc(2099),
        tokenFamilyId: 'f',
        silentRefreshWindowMinutes: 5,
      );
  @override
  Future<void> save(DeviceCredentials credentials) async {}
  @override
  Future<void> clear() async {}
}

class _Sec implements DeviceSecurityStateStore {
  @override
  Future<bool> requiresReauth() async => false;
  @override
  Future<void> setRequiresReauth(bool value) async {}
}

class _Gw implements PosGateway {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

class _Term implements CardTerminalGateway {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

class _Printer implements ReceiptPrinter {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

class _Data implements LocalDataProtectionService {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}
