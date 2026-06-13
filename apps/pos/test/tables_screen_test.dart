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
import 'package:pos_app/src/features/tables/tables_screen.dart';

void main() {
  testWidgets('groups tables by zone and renders tiles', (tester) async {
    tester.view.physicalSize = const Size(900, 1400);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    final database = PosDatabase.memory();
    final controller = await _build(database, _Gw());

    await tester.pumpWidget(
      MaterialApp(
        theme: PosTheme.light(),
        home: Scaffold(body: TablesScreen(controller: controller)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Patio'), findsOneWidget);
    expect(find.text('T1'), findsOneWidget);
    expect(find.text('T2'), findsOneWidget);
  });

  testWidgets('claiming an available table calls claimDiningTable', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(900, 1400);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    final database = PosDatabase.memory();
    final gateway = _Gw();
    final controller = await _build(database, gateway);

    await tester.pumpWidget(
      MaterialApp(
        theme: PosTheme.light(),
        home: Scaffold(body: TablesScreen(controller: controller)),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.text('T1'));
    await tester.pumpAndSettle();

    expect(find.text('Claim table'), findsOneWidget);
    await tester.enterText(
      find.widgetWithText(TextField, 'Party name'),
      'Smith',
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Claim'));
    // Claiming starts a periodic lease-heartbeat timer, so we pump fixed frames
    // (pumpAndSettle would hang waiting for the timer to settle).
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));

    expect(gateway.claimedTableId, 'tbl-1');
    expect(gateway.claimedParty, 'Smith');

    // Cancel the heartbeat before the test ends.
    controller.dispose();
    await database.close();
  });
}

Future<PosHomeController> _build(
  PosDatabase database,
  PosGateway gateway,
) async {
  await database
      .into(database.deviceBootstrapCaches)
      .insert(
        DeviceBootstrapCachesCompanion.insert(
          id: 'b1',
          apiBaseUrl: const Value('https://example.test'),
          deviceId: const Value('device-1'),
          merchantId: const Value('merchant-1'),
          storeId: const Value('store-1'),
          payloadJson: '''
{
  "bootstrap": {"merchant": {"id": "merchant-1"}, "store": {"id": "store-1"}, "device": {"id": "device-1"}, "register": {"active_session": null}},
  "config": {"items": [], "tax_rules": [], "discount_rules": [], "membership_plans": [], "payment_capabilities": {"default_provider": "x", "supported_tenders": ["cash"], "terminal_capabilities": {}}, "print_routes": []},
  "tables": {"tables": [
    {"id": "tbl-1", "name": "T1", "capacity": 4, "status": "available", "zone_name": "Patio", "lease": {"is_claimed_by_current_device": false}},
    {"id": "tbl-2", "name": "T2", "capacity": 2, "status": "available", "zone_name": "Patio", "lease": {"is_claimed_by_current_device": false}}
  ]},
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
    gateway: gateway,
    terminalGateway: _Term(),
    receiptPrinter: _Printer(),
    dataProtectionService: _Data(),
  );
  await controller.load();
  return controller;
}

class _Gw implements PosGateway {
  String? claimedTableId;
  String? claimedParty;

  @override
  Future<DiningTableSnapshot> claimDiningTable({
    required String diningTableId,
    String? currentPartyName,
    int? guestCount,
  }) async {
    claimedTableId = diningTableId;
    claimedParty = currentPartyName;
    return DiningTableSnapshot(
      id: diningTableId,
      name: 'T1',
      capacity: 4,
      status: 'occupied',
      lease: const DiningTableLeaseSnapshot(isClaimedByCurrentDevice: true),
      zoneName: 'Patio',
      currentPartyName: currentPartyName,
    );
  }

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
