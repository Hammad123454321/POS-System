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
import 'package:pos_app/src/features/appointments/appointments_screen.dart';
import 'package:pos_app/src/features/home/pos_home_controller.dart';

void main() {
  testWidgets('renders staff column and a same-day appointment', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(900, 1400);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);

    final today = DateTime.now();
    final start = DateTime(
      today.year,
      today.month,
      today.day,
      10,
    ).toUtc().toIso8601String();
    final end = DateTime(
      today.year,
      today.month,
      today.day,
      10,
      30,
    ).toUtc().toIso8601String();

    final database = PosDatabase.memory();
    final gateway = _Gw();
    final controller = await _build(database, gateway, start, end);

    await tester.pumpWidget(
      MaterialApp(
        theme: PosTheme.light(),
        home: AppointmentsScreen(controller: controller),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Alex Stylist'), findsOneWidget);
    expect(find.text('Jane Doe'), findsOneWidget);

    // Open the appointment sheet and check in.
    await tester.tap(find.text('Jane Doe'));
    await tester.pumpAndSettle();
    expect(find.text('Check in'), findsOneWidget);

    await tester.tap(find.text('Check in'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));

    expect(gateway.checkedInId, 'appt-1');

    controller.dispose();
    await database.close();
  });
}

Future<PosHomeController> _build(
  PosDatabase database,
  PosGateway gateway,
  String start,
  String end,
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
          payloadJson:
              '''
{
  "bootstrap": {"merchant": {"id": "merchant-1"}, "store": {"id": "store-1"}, "device": {"id": "device-1"}, "register": {"active_session": null}},
  "config": {"items": [], "tax_rules": [], "discount_rules": [], "membership_plans": [], "payment_capabilities": {"default_provider": "x", "supported_tenders": ["cash"], "terminal_capabilities": {}}, "print_routes": []},
  "tables": {"tables": []},
  "workforce": {
    "staff": [{"id": "staff-1", "display_name": "Alex Stylist"}],
    "appointments": [{"id": "appt-1", "status": "confirmed", "starts_at": "$start", "ends_at": "$end", "staff_profile_id": "staff-1", "customer_name": "Jane Doe"}]
  },
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
  String? checkedInId;

  @override
  Future<AppointmentSnapshot> checkInAppointment(String appointmentId) async {
    checkedInId = appointmentId;
    return AppointmentSnapshot(
      id: appointmentId,
      status: 'checked_in',
      startsAt: '',
      endsAt: '',
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
