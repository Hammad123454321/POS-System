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
import 'package:pos_app/src/features/checkout/receipt_confirmation_screen.dart';
import 'package:pos_app/src/features/checkout/tender_screen.dart';
import 'package:pos_app/src/features/home/pos_home_controller.dart';

void main() {
  testWidgets('cash checkout success pushes the receipt confirmation',
      (tester) async {
    tester.view.physicalSize = const Size(900, 1400);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    final database = PosDatabase.memory();
    final gateway = _ScriptedGateway();
    final controller = await _build(database, gateway);

    // Put an item in the cart so there is a total to charge.
    controller.addItem(controller.catalogItems.first);

    await tester.pumpWidget(MaterialApp(
      theme: PosTheme.light(),
      home: TenderScreen(controller: controller),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Complete cash sale'), findsOneWidget);

    await tester.enterText(
      find.widgetWithText(TextField, 'Cash tendered (minor units)'),
      '5000',
    );
    await tester.tap(find.text('Complete cash sale'));
    await tester.pumpAndSettle();

    expect(find.byType(ReceiptConfirmationScreen), findsOneWidget);
    expect(find.text('Sale complete'), findsOneWidget);

    controller.dispose();
    await database.close();
  });
}

Future<PosHomeController> _build(
  PosDatabase database,
  PosGateway gateway,
) async {
  await database.into(database.deviceBootstrapCaches).insert(
        DeviceBootstrapCachesCompanion.insert(
          id: 'b1',
          apiBaseUrl: const Value('https://example.test'),
          deviceId: const Value('device-1'),
          merchantId: const Value('merchant-1'),
          storeId: const Value('store-1'),
          payloadJson: '''
{
  "bootstrap": {"merchant": {"id": "merchant-1"}, "store": {"id": "store-1"}, "device": {"id": "device-1"}, "register": {"active_session": {"id": "s1", "status": "open", "business_date": "2026-04-24", "session_version": 1, "opening_float_minor": 0, "expected_cash_minor": 0}}},
  "config": {
    "items": [{"id": "i1", "name": "Latte", "currency": "USD", "effective_price_minor": 450, "sold_out": false, "category_id": "c1", "category_name": "Drinks", "sku": "LAT"}],
    "tax_rules": [], "discount_rules": [], "membership_plans": [],
    "payment_capabilities": {"default_provider": "fiserv_bluepay", "supported_tenders": ["cash", "card"], "terminal_capabilities": {}},
    "print_routes": []
  },
  "tables": {"tables": []},
  "business_day_summary": {"business_date": "2026-04-24", "open_orders_count": 0, "paid_orders_count": 0, "gross_sales_minor": 0, "cash_sales_minor": 0, "open_register_sessions_count": 1, "open_exception_cases_count": 0},
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
    gateway: gateway,
    terminalGateway: _Term(),
    receiptPrinter: _Printer(),
    dataProtectionService: _Data(),
  );
  await controller.load();
  return controller;
}

class _ScriptedGateway implements PosGateway {
  @override
  Future<OrderSummary> createOrder({
    required String registerSessionId,
    required List<Map<String, dynamic>> lines,
    String? customerId,
    String? discountRuleId,
  }) async =>
      const OrderSummary(id: 'o1', orderNumber: 'A-1', totalMinor: 450);

  @override
  Future<ReceiptSummary> tenderOrder({
    required String orderId,
    required List<Map<String, dynamic>> tenders,
  }) async =>
      const ReceiptSummary(
        receiptId: 'r1',
        receiptNumber: 'RCP-1',
        payload: {'total_minor': 450, 'currency': 'USD', 'payments': []},
      );

  // The post-checkout cloud refresh fans out to many endpoints; for this widget
  // test we let those be no-ops so the receipt-confirmation navigation is the
  // only observable effect.
  @override
  noSuchMethod(Invocation invocation) async => null;
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

class _Term implements CardTerminalGateway {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

class _Printer implements ReceiptPrinter {
  @override
  Future<void> printReceipt({
    required String receiptId,
    required String routeKey,
    required List<String> printerNames,
    required Map<String, dynamic> payload,
  }) async {}
}

class _Data implements LocalDataProtectionService {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}
