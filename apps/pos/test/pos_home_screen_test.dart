import 'package:drift/drift.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/core/models/pos_models.dart';
import 'package:pos_app/src/core/services/device_credentials_store.dart';
import 'package:pos_app/src/core/services/device_security_state_store.dart';
import 'package:pos_app/src/core/services/local_data_protection_service.dart';
import 'package:pos_app/src/core/services/local_encryption_key_store.dart';
import 'package:pos_app/src/core/services/pax_terminal_gateway.dart';
import 'package:pos_app/src/core/services/receipt_printer.dart';
import 'package:pos_app/src/data/local/pos_database.dart';
import 'package:pos_app/src/data/remote/pos_gateway.dart';
import 'package:pos_app/src/data/repositories/bootstrap_cache_repository.dart';
import 'package:pos_app/src/data/repositories/receipt_print_queue_repository.dart';
import 'package:pos_app/src/data/repositories/sync_outbox_repository.dart';
import 'package:pos_app/src/features/home/pos_home_controller.dart';
import 'package:pos_app/src/features/home/pos_home_screen.dart';

void main() {
  testWidgets('renders the enrolled phase 1b operating screen', (tester) async {
    final database = PosDatabase.memory();

    await database
        .into(database.deviceBootstrapCaches)
        .insert(
          DeviceBootstrapCachesCompanion.insert(
            id: 'bootstrap-1',
            apiBaseUrl: const Value('https://example.test'),
            deviceId: const Value('device-1'),
            merchantId: const Value('merchant-1'),
            storeId: const Value('store-1'),
            payloadJson: '''
{
  "bootstrap": {
    "merchant": {"id": "merchant-1"},
    "store": {"id": "store-1"},
    "device": {"id": "device-1"},
    "register": {
      "active_session": {
        "id": "session-1",
        "status": "open",
        "business_date": "2026-04-21",
        "session_version": 2,
        "opening_float_minor": 10000,
        "expected_cash_minor": 12500
      }
    }
  },
  "config": {
    "items": [
      {
        "id": "item-1",
        "name": "Burger",
        "currency": "USD",
        "effective_price_minor": 1200,
        "sold_out": false,
        "tax_rule_id": "tax-1"
      }
    ],
    "tax_rules": [
      {
        "id": "tax-1",
        "name": "Sales Tax",
        "rate_basis_points": 1000,
        "is_inclusive": false
      }
    ],
    "discount_rules": [
      {
        "id": "discount-1",
        "name": "VIP 10%",
        "code": "VIP10",
        "type": "percent_basis_points",
        "value_basis_points": 1000
      }
    ],
    "membership_plans": [
      {
        "id": "membership-plan-1",
        "name": "Monthly VIP",
        "code": "VIP-MONTHLY",
        "price_minor": 2999,
        "currency": "USD",
        "duration_days": 30
      }
    ],
    "payment_capabilities": {
      "default_provider": "fiserv_bluepay",
      "supported_tenders": ["cash", "card", "gift_card"],
      "terminal_capabilities": {
        "provider_key": "fiserv_bluepay",
        "integration_mode": "semi_integrated",
        "supports_refund": true,
        "supports_void": true
      }
    },
    "print_routes": [
      {
        "id": "route-1",
        "route_key": "receipt-default",
        "document_type": "receipt",
        "primary_printer": {
          "id": "printer-1",
          "name": "Front Receipt Printer",
          "driver_key": "network-escpos"
        }
      }
    ]
  },
  "tables": {
    "tables": [
      {
        "id": "table-1",
        "name": "Table 1",
        "zone_name": "Main Floor",
        "capacity": 4,
        "status": "occupied",
        "current_party_name": "A Khan",
        "guest_count": 2,
        "assigned_device_id": "device-1",
        "lease": {
          "lease_version": 1,
          "current_holder_device_id": "device-1",
          "lease_expires_at": "2026-04-21T12:05:00Z",
          "is_claimed_by_current_device": true
        }
      }
    ]
  },
  "business_day_summary": {
    "business_date": "2026-04-21",
    "open_orders_count": 1,
    "paid_orders_count": 3,
    "gross_sales_minor": 5400,
    "cash_sales_minor": 5400,
    "open_register_sessions_count": 1,
    "open_exception_cases_count": 1
  },
  "exceptions": {
    "count": 1,
    "cases": [
      {
        "id": "exception-1",
        "type": "register_session.close_variance",
        "severity": "medium",
        "message": "Variance review required."
      }
    ]
  },
  "deltas": {
    "cursor": "2026-04-21T12:00:00Z"
  }
}
''',
            syncedAt: DateTime(2026, 4, 21, 12),
          ),
        );

    final controller = PosHomeController(
      bootstrapCacheRepository: BootstrapCacheRepository(database),
      syncOutboxRepository: SyncOutboxRepository(database),
      receiptPrintQueueRepository: ReceiptPrintQueueRepository(database),
      credentialsStore: _FakeDeviceCredentialsStore(
        DeviceCredentials(
          apiBaseUrl: 'https://example.test',
          deviceId: 'device-1',
          accessToken: 'access-token',
          accessTokenExpiresAt: DateTime.utc(2026, 4, 21, 13),
          refreshToken: 'refresh-token',
          refreshTokenExpiresAt: DateTime.utc(2026, 5, 21, 13),
          tokenFamilyId: 'family-1',
          silentRefreshWindowMinutes: 5,
        ),
      ),
      securityStateStore: _FakeDeviceSecurityStateStore(),
      gateway: _FakePosGateway(),
      terminalGateway: _FakeCardTerminalGateway(),
      receiptPrinter: _FakeReceiptPrinter(),
      dataProtectionService: LocalDataProtectionService(
        keyStore: LocalEncryptionKeyStore(
          backend: _InMemoryLocalEncryptionBackend(),
          clock: () => DateTime.utc(2026, 4, 21, 12),
        ),
        database: database,
        clock: () => DateTime.utc(2026, 4, 21, 12),
      ),
    );

    await controller.load();

    await tester.pumpWidget(
      MaterialApp(home: PosHomeScreen(controller: controller)),
    );

    expect(find.text('Store Operations Console'), findsOneWidget);
    expect(find.text('Device Status'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Business Day'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Business Day'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Dining Tables'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Dining Tables'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Customer & Discount'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Customer & Discount'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Stored Value & Memberships'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Stored Value & Memberships'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Catalog & Tendering'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Catalog & Tendering'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Burger'),
      250,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Burger'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Print Routes'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Print Routes'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.text('Sync Recovery'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Sync Recovery'), findsOneWidget);

    controller.dispose();
    await database.close();
  });
}

class _FakeDeviceCredentialsStore implements DeviceCredentialsStore {
  _FakeDeviceCredentialsStore(this._credentials);

  DeviceCredentials? _credentials;

  @override
  Future<void> clear() async {
    _credentials = null;
  }

  @override
  Future<DeviceCredentials?> load() async => _credentials;

  @override
  Future<void> save(DeviceCredentials credentials) async {
    _credentials = credentials;
  }
}

class _FakeReceiptPrinter implements ReceiptPrinter {
  @override
  Future<void> printReceipt({
    required String receiptId,
    required String routeKey,
    required List<String> printerNames,
    required Map<String, dynamic> payload,
  }) async {}
}

class _FakeDeviceSecurityStateStore implements DeviceSecurityStateStore {
  bool _requiresReauth = false;

  @override
  Future<bool> requiresReauth() async => _requiresReauth;

  @override
  Future<void> setRequiresReauth(bool value) async {
    _requiresReauth = value;
  }
}

class _InMemoryLocalEncryptionBackend implements LocalEncryptionBackend {
  LocalEncryptionKeyState? _state;

  @override
  Future<LocalEncryptionKeyState> beginRotation({
    required String reason,
    required DateTime now,
  }) async {
    final current = await loadOrCreateState(now: now);

    if (current.pending != null) {
      return current;
    }

    _state = LocalEncryptionKeyState(
      active: current.active,
      pending: LocalEncryptionKeyMaterial(
        key: 'pending-key-$reason',
        version: current.active.version + 1,
        createdAt: now,
        reason: reason,
      ),
    );

    return _state!;
  }

  @override
  Future<LocalEncryptionKeyState> cancelRotation() async {
    final current = _state!;
    _state = LocalEncryptionKeyState(active: current.active);

    return _state!;
  }

  @override
  Future<LocalEncryptionKeyState> commitRotation() async {
    final current = _state!;
    _state = LocalEncryptionKeyState(active: current.pending ?? current.active);

    return _state!;
  }

  @override
  Future<LocalEncryptionKeyState> loadOrCreateState({
    required DateTime now,
  }) async {
    _state ??= LocalEncryptionKeyState(
      active: LocalEncryptionKeyMaterial(
        key: 'active-key',
        version: 1,
        createdAt: now,
      ),
    );

    return _state!;
  }
}

class _FakePosGateway implements PosGateway {
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);

  @override
  Future<MembershipLookupSnapshot> activateMembership({
    required String customerId,
    required String membershipPlanId,
    String? memberNumber,
  }) async {
    return MembershipLookupSnapshot(
      memberAccountId: 'member-account-1',
      memberNumber: memberNumber ?? 'MEM-1001',
      status: 'active',
      customerId: customerId,
      customerName: 'VIP Guest',
      membershipPlan: const MembershipPlanSnapshot(
        id: 'membership-plan-1',
        name: 'Monthly VIP',
        code: 'VIP-MONTHLY',
        priceMinor: 2999,
        currency: 'USD',
        durationDays: 30,
      ),
    );
  }

  @override
  Future<Map<String, dynamic>> bootstrap() async => const {};

  @override
  Future<ReceiptSummary> cashCheckout({
    required String orderId,
    required int tenderedMinor,
  }) async {
    throw UnimplementedError();
  }

  @override
  Future<DiningTableSnapshot> claimDiningTable({
    required String diningTableId,
    String? currentPartyName,
    int? guestCount,
  }) async {
    throw UnimplementedError();
  }

  @override
  void close() {}

  @override
  Future<RegisterSessionSnapshot> closeRegisterSession({
    required String registerSessionId,
    required int countedCashMinor,
    required int sessionVersion,
  }) async {
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> config() async => const {};

  @override
  Future<OrderSummary> createOrder({
    required String registerSessionId,
    required List<Map<String, dynamic>> lines,
    String? customerId,
    String? discountRuleId,
  }) async {
    throw UnimplementedError();
  }

  @override
  Future<DeviceCredentials> enroll({
    required String apiBaseUrl,
    required String enrollmentCode,
    required String deviceName,
  }) async {
    throw UnimplementedError();
  }

  @override
  Future<SyncRecoveryRunSnapshot> getSyncRecoveryRun(
    String recoveryRunId,
  ) async {
    return SyncRecoveryRunSnapshot(
      id: recoveryRunId,
      status: 'completed',
      eventCount: 1,
      batchId: 'batch-1',
      batch: const SyncRecoveryBatchSnapshot(
        id: 'batch-1',
        name: 'sync-recovery',
        totalJobs: 1,
        pendingJobs: 0,
        failedJobs: 0,
        processedJobs: 1,
        progress: 100,
        cancelled: false,
        finished: true,
      ),
    );
  }

  @override
  Future<BusinessDaySummarySnapshot> getBusinessDaySummary() async {
    return const BusinessDaySummarySnapshot(
      businessDate: '2026-04-21',
      openOrdersCount: 1,
      paidOrdersCount: 1,
      grossSalesMinor: 1000,
      cashSalesMinor: 1000,
      openRegisterSessionsCount: 1,
      openExceptionCasesCount: 0,
    );
  }

  @override
  Future<OpenExceptionSummary> getOpenExceptions() async {
    return const OpenExceptionSummary(count: 0, cases: []);
  }

  @override
  Future<DiningTableSnapshot> heartbeatDiningTable(String diningTableId) async {
    throw UnimplementedError();
  }

  @override
  Future<GiftCardSnapshot> issueGiftCard({
    required int amountMinor,
    String? customerId,
    String? requestedCode,
  }) async {
    return GiftCardSnapshot(
      id: 'gift-card-1',
      code: requestedCode ?? 'GC1001',
      currency: 'USD',
      status: 'active',
      currentBalanceMinor: amountMinor,
      issuedToCustomerId: customerId,
      labelPayload: const {
        'document_type': 'label',
        'gift_card_code': 'GC1001',
        'balance_minor': 3000,
      },
    );
  }

  @override
  Future<List<DiningTableSnapshot>> listDiningTables() async => const [];

  @override
  Future<GiftCardSnapshot> lookupGiftCard(String code) async {
    return GiftCardSnapshot(
      id: 'gift-card-1',
      code: code,
      currency: 'USD',
      status: 'active',
      currentBalanceMinor: 3000,
    );
  }

  @override
  Future<MembershipLookupSnapshot> lookupMembership({
    String? memberNumber,
    String? customerId,
  }) async {
    return MembershipLookupSnapshot(
      memberAccountId: 'member-account-1',
      memberNumber: memberNumber ?? 'MEM-1001',
      status: 'active',
      customerId: customerId,
      customerName: 'VIP Guest',
      membershipPlan: const MembershipPlanSnapshot(
        id: 'membership-plan-1',
        name: 'Monthly VIP',
        code: 'VIP-MONTHLY',
        priceMinor: 2999,
        currency: 'USD',
        durationDays: 30,
      ),
    );
  }

  @override
  Future<RegisterSessionSnapshot> openRegisterSession(
    int openingFloatMinor,
  ) async {
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> pullSyncDeltas(String? cursor) async => const {};

  @override
  Future<List<Map<String, dynamic>>> pushSyncEvents(
    List<SyncOutboxEntryData> events,
  ) async {
    return const [];
  }

  @override
  Future<RefundSummary> refundPayment({
    required String paymentId,
    int? amountMinor,
    String? reason,
  }) async {
    return RefundSummary(
      refundId: 'refund-1',
      paymentId: paymentId,
      status: 'refunded',
      amountMinor: amountMinor ?? 0,
      reason: reason,
    );
  }

  @override
  Future<DiningTableSnapshot> releaseDiningTable(String diningTableId) async {
    throw UnimplementedError();
  }

  @override
  Future<List<CustomerSummary>> searchCustomers(String query) async => const [];

  @override
  Future<SyncRecoveryRunSnapshot> startSyncRecoveryRun() async {
    return SyncRecoveryRunSnapshot(
      id: 'recovery-run-1',
      status: 'completed',
      eventCount: 1,
    );
  }

  @override
  Future<ReceiptSummary> tenderOrder({
    required String orderId,
    required List<Map<String, dynamic>> tenders,
  }) async {
    throw UnimplementedError();
  }

  @override
  Future<GiftCardSnapshot> topUpGiftCard({
    required String giftCardCode,
    required int amountMinor,
  }) async {
    return GiftCardSnapshot(
      id: 'gift-card-1',
      code: giftCardCode,
      currency: 'USD',
      status: 'active',
      currentBalanceMinor: amountMinor,
    );
  }

  @override
  Future<VoidSummary> voidPayment({
    required String paymentId,
    String? reason,
  }) async {
    return VoidSummary(
      voidRecordId: 'void-1',
      paymentId: paymentId,
      status: 'voided',
      reason: reason,
    );
  }
}

class _FakeCardTerminalGateway implements CardTerminalGateway {
  @override
  Future<TerminalCheckoutResult> checkoutCard({
    required String orderId,
    required int amountMinor,
    required int tipMinor,
    String? terminalReference,
  }) async {
    return TerminalCheckoutResult(
      status: TerminalCheckoutStatus.approved,
      providerKey: 'fiserv_bluepay',
      providerTransactionId: '100001',
      authCode: 'ALVSGO',
      maskedPan: '************1111',
      terminalId: 'PAX-A920-01',
      entryMode: 'chip',
      applicationLabel: 'VISA CREDIT',
      aid: 'A0000000031010',
      tvr: '0000008000',
      tsi: 'E800',
      terminalStatusCode: 'approved',
      terminalResultCode: '00',
      terminalTimestamp: DateTime.now().toUtc(),
      terminalReference: terminalReference,
    );
  }
}
