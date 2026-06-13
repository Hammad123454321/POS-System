import 'package:drift/drift.dart' show Value;
import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/core/models/pos_models.dart';
import 'package:pos_app/src/core/services/device_credentials_store.dart';
import 'package:pos_app/src/core/services/device_security_state_store.dart';
import 'package:pos_app/src/core/services/local_data_protection_service.dart';
import 'package:pos_app/src/core/services/local_encryption_key_store.dart';
import 'package:pos_app/src/core/hardware/cash_drawer.dart';
import 'package:pos_app/src/core/services/pax_terminal_gateway.dart';
import 'package:pos_app/src/core/services/receipt_printer.dart';
import 'package:pos_app/src/data/local/pos_database.dart';
import 'package:pos_app/src/data/remote/pos_gateway.dart';
import 'package:pos_app/src/data/repositories/bootstrap_cache_repository.dart';
import 'package:pos_app/src/data/repositories/receipt_print_queue_repository.dart';
import 'package:pos_app/src/data/repositories/sync_outbox_repository.dart';
import 'package:pos_app/src/features/home/pos_home_controller.dart';

void main() {
  test(
    'checkoutCard sends enriched terminal-approved payload only after approval',
    () async {
      final database = PosDatabase.memory();
      await _seedBootstrap(database);

      final gateway = _CapturePosGateway();
      final controller = PosHomeController(
        bootstrapCacheRepository: BootstrapCacheRepository(database),
        syncOutboxRepository: SyncOutboxRepository(database),
        receiptPrintQueueRepository: ReceiptPrintQueueRepository(database),
        credentialsStore: _FakeDeviceCredentialsStore(_credentials()),
        securityStateStore: _FakeDeviceSecurityStateStore(),
        gateway: gateway,
        terminalGateway: _FakeCardTerminalGateway(
          TerminalCheckoutResult(
            status: TerminalCheckoutStatus.approved,
            providerKey: 'fiserv_bluepay',
            providerTransactionId: '100000111',
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
            terminalTimestamp: DateTime.utc(2026, 4, 24, 12),
            terminalReference: 'PAX-REF-001',
          ),
        ),
        receiptPrinter: _FakeReceiptPrinter(),
        dataProtectionService: LocalDataProtectionService(
          keyStore: LocalEncryptionKeyStore(
            backend: _InMemoryLocalEncryptionBackend(),
            clock: () => DateTime.utc(2026, 4, 24, 12),
          ),
          database: database,
          clock: () => DateTime.utc(2026, 4, 24, 12),
        ),
      );

      await controller.load();
      controller.addItem(controller.catalogItems.first);
      await controller.checkoutCard(150);

      expect(gateway.tenderCalls, 1);
      expect(gateway.lastTenders, isNotNull);
      expect(gateway.lastTenders!.first['method'], 'card');
      expect(gateway.lastTenders!.first['provider_key'], 'fiserv_bluepay');
      expect(
        gateway.lastTenders!.first['provider_transaction_id'],
        '100000111',
      );
      expect(gateway.lastTenders!.first['auth_code'], 'ALVSGO');
      expect(gateway.lastTenders!.first['terminal_result_code'], '00');
      expect(gateway.lastTenders!.first['terminal_status_code'], 'approved');

      controller.dispose();
      await database.close();
    },
  );

  test(
    'checkoutCard keeps an in-doubt lock and blocks immediate retry',
    () async {
      final database = PosDatabase.memory();
      await _seedBootstrap(database);

      final gateway = _CapturePosGateway();
      final controller = PosHomeController(
        bootstrapCacheRepository: BootstrapCacheRepository(database),
        syncOutboxRepository: SyncOutboxRepository(database),
        receiptPrintQueueRepository: ReceiptPrintQueueRepository(database),
        credentialsStore: _FakeDeviceCredentialsStore(_credentials()),
        securityStateStore: _FakeDeviceSecurityStateStore(),
        gateway: gateway,
        terminalGateway: _FakeCardTerminalGateway(
          TerminalCheckoutResult(
            status: TerminalCheckoutStatus.inDoubt,
            providerKey: 'fiserv_bluepay',
            providerTransactionId: '100000222',
            authCode: 'TIMEOUT',
            maskedPan: '************1111',
            terminalId: 'PAX-A920-02',
            entryMode: 'chip',
            applicationLabel: 'VISA CREDIT',
            aid: 'A0000000031010',
            tvr: '0000008000',
            tsi: 'E800',
            terminalStatusCode: 'timeout',
            terminalResultCode: 'no_response',
            terminalTimestamp: DateTime.utc(2026, 4, 24, 12),
            terminalReference: 'PAX-REF-002',
          ),
        ),
        receiptPrinter: _FakeReceiptPrinter(),
        dataProtectionService: LocalDataProtectionService(
          keyStore: LocalEncryptionKeyStore(
            backend: _InMemoryLocalEncryptionBackend(),
            clock: () => DateTime.utc(2026, 4, 24, 12),
          ),
          database: database,
          clock: () => DateTime.utc(2026, 4, 24, 12),
        ),
      );

      await controller.load();
      controller.addItem(controller.catalogItems.first);
      await controller.checkoutCard(0);

      expect(controller.inDoubtOrderId, isNotNull);
      expect(gateway.createOrderCalls, 1);
      expect(gateway.tenderCalls, 0);

      await controller.checkoutCard(0);

      expect(gateway.createOrderCalls, 1);
      expect(gateway.tenderCalls, 0);
      expect(controller.errorMessage, contains('Card recovery is required'));

      controller.dispose();
      await database.close();
    },
  );

  test('checkoutCash opens the cash drawer on success', () async {
    final database = PosDatabase.memory();
    await _seedBootstrap(database);

    final drawer = DebugCashDrawer();
    final controller = PosHomeController(
      bootstrapCacheRepository: BootstrapCacheRepository(database),
      syncOutboxRepository: SyncOutboxRepository(database),
      receiptPrintQueueRepository: ReceiptPrintQueueRepository(database),
      credentialsStore: _FakeDeviceCredentialsStore(_credentials()),
      securityStateStore: _FakeDeviceSecurityStateStore(),
      gateway: _CapturePosGateway(),
      terminalGateway: _FakeCardTerminalGateway(
        TerminalCheckoutResult(
          status: TerminalCheckoutStatus.approved,
          providerKey: 'fiserv_bluepay',
          providerTransactionId: '1',
          authCode: 'A',
          maskedPan: '************1111',
          terminalId: 'PAX',
          entryMode: 'chip',
          applicationLabel: 'VISA',
          aid: 'A0',
          tvr: '0',
          tsi: '0',
          terminalStatusCode: 'approved',
          terminalResultCode: '00',
          terminalTimestamp: DateTime.utc(2026, 4, 24, 12),
          terminalReference: 'REF',
        ),
      ),
      receiptPrinter: _FakeReceiptPrinter(),
      dataProtectionService: LocalDataProtectionService(
        keyStore: LocalEncryptionKeyStore(
          backend: _InMemoryLocalEncryptionBackend(),
          clock: () => DateTime.utc(2026, 4, 24, 12),
        ),
        database: database,
        clock: () => DateTime.utc(2026, 4, 24, 12),
      ),
      cashDrawer: drawer,
    );

    await controller.load();
    controller.addItem(controller.catalogItems.first);
    await controller.checkoutCash(1500);

    expect(drawer.openCount, 1);

    controller.dispose();
    await database.close();
  });

  test('noSaleOpenDrawer opens the drawer and queues an audit event', () async {
    final database = PosDatabase.memory();
    await _seedBootstrap(database);

    final drawer = DebugCashDrawer();
    final outbox = SyncOutboxRepository(database);
    final controller = PosHomeController(
      bootstrapCacheRepository: BootstrapCacheRepository(database),
      syncOutboxRepository: outbox,
      receiptPrintQueueRepository: ReceiptPrintQueueRepository(database),
      credentialsStore: _FakeDeviceCredentialsStore(_credentials()),
      securityStateStore: _FakeDeviceSecurityStateStore(),
      gateway: _CapturePosGateway(),
      terminalGateway: _FakeCardTerminalGateway(
        TerminalCheckoutResult(
          status: TerminalCheckoutStatus.approved,
          providerKey: 'fiserv_bluepay',
          providerTransactionId: '1',
          authCode: 'A',
          maskedPan: '************1111',
          terminalId: 'PAX',
          entryMode: 'chip',
          applicationLabel: 'VISA',
          aid: 'A0',
          tvr: '0',
          tsi: '0',
          terminalStatusCode: 'approved',
          terminalResultCode: '00',
          terminalTimestamp: DateTime.utc(2026, 4, 24, 12),
          terminalReference: 'REF',
        ),
      ),
      receiptPrinter: _FakeReceiptPrinter(),
      dataProtectionService: LocalDataProtectionService(
        keyStore: LocalEncryptionKeyStore(
          backend: _InMemoryLocalEncryptionBackend(),
          clock: () => DateTime.utc(2026, 4, 24, 12),
        ),
        database: database,
        clock: () => DateTime.utc(2026, 4, 24, 12),
      ),
      cashDrawer: drawer,
    );

    await controller.load();
    await controller.noSaleOpenDrawer();

    expect(drawer.openCount, 1);
    final pending = await outbox.pending();
    expect(
      pending.any((e) => e.action == 'no_sale_drawer_open'),
      isTrue,
    );

    controller.dispose();
    await database.close();
  });
}

Future<void> _seedBootstrap(PosDatabase database) {
  return database
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
        "business_date": "2026-04-24",
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
        "sold_out": false
      }
    ],
    "tax_rules": [],
    "discount_rules": [],
    "membership_plans": [],
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
    "print_routes": []
  },
  "tables": {"tables": []},
  "business_day_summary": {
    "business_date": "2026-04-24",
    "open_orders_count": 0,
    "paid_orders_count": 0,
    "gross_sales_minor": 0,
    "cash_sales_minor": 0,
    "open_register_sessions_count": 1,
    "open_exception_cases_count": 0
  },
  "exceptions": {"count": 0, "cases": []},
  "deltas": {"cursor": "2026-04-24T12:00:00Z"}
}
''',
          syncedAt: DateTime(2026, 4, 24, 12),
        ),
      );
}

DeviceCredentials _credentials() {
  return DeviceCredentials(
    apiBaseUrl: 'https://example.test',
    deviceId: 'device-1',
    accessToken: 'access-token',
    accessTokenExpiresAt: DateTime.utc(2026, 4, 24, 13),
    refreshToken: 'refresh-token',
    refreshTokenExpiresAt: DateTime.utc(2026, 5, 24, 13),
    tokenFamilyId: 'family-1',
    silentRefreshWindowMinutes: 5,
  );
}

class _CapturePosGateway implements PosGateway {
  int createOrderCalls = 0;
  int tenderCalls = 0;
  List<Map<String, dynamic>>? lastTenders;

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);

  @override
  Future<OrderSummary> createOrder({
    required String registerSessionId,
    required List<Map<String, dynamic>> lines,
    String? customerId,
    String? discountRuleId,
  }) async {
    createOrderCalls++;

    return const OrderSummary(
      id: 'order-1',
      orderNumber: 'ORD-1001',
      totalMinor: 1200,
    );
  }

  @override
  Future<ReceiptSummary> tenderOrder({
    required String orderId,
    required List<Map<String, dynamic>> tenders,
  }) async {
    tenderCalls++;
    lastTenders = tenders;

    return ReceiptSummary(
      receiptId: 'receipt-1',
      receiptNumber: 'RCP-1001',
      payload: {
        'total_minor': 1200,
        'tip_minor': tenders.first['tip_minor'] ?? 0,
        'paid_minor': 1200 + (tenders.first['tip_minor'] as int? ?? 0),
        'payments': [
          {
            'payment_id': 'payment-1',
            'method': tenders.first['method'],
            'status': 'captured',
            'amount_minor': 1200 + (tenders.first['tip_minor'] as int? ?? 0),
            'applied_minor': 1200,
            'tip_minor': tenders.first['tip_minor'] ?? 0,
            'tendered_minor': 1200,
            'change_minor': 0,
          },
        ],
      },
    );
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
  Future<Map<String, dynamic>> bootstrap() async => const {};

  @override
  Future<Map<String, dynamic>> config() async => const {};

  @override
  Future<Map<String, dynamic>> pullSyncDeltas(String? cursor) async => const {};

  @override
  Future<List<Map<String, dynamic>>> pushSyncEvents(
    List<SyncOutboxEntryData> events,
  ) async => const [];

  @override
  Future<RegisterSessionSnapshot> openRegisterSession(int openingFloatMinor) {
    throw UnimplementedError();
  }

  @override
  Future<RegisterSessionSnapshot> closeRegisterSession({
    required String registerSessionId,
    required int countedCashMinor,
    required int sessionVersion,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<List<CustomerSummary>> searchCustomers(String query) async => const [];

  @override
  Future<List<DiningTableSnapshot>> listDiningTables() async => const [];

  @override
  Future<DiningTableSnapshot> claimDiningTable({
    required String diningTableId,
    String? currentPartyName,
    int? guestCount,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<DiningTableSnapshot> heartbeatDiningTable(String diningTableId) {
    throw UnimplementedError();
  }

  @override
  Future<DiningTableSnapshot> releaseDiningTable(String diningTableId) {
    throw UnimplementedError();
  }

  @override
  Future<BusinessDaySummarySnapshot> getBusinessDaySummary() async {
    return const BusinessDaySummarySnapshot(
      businessDate: '2026-04-24',
      openOrdersCount: 0,
      paidOrdersCount: 0,
      grossSalesMinor: 0,
      cashSalesMinor: 0,
      openRegisterSessionsCount: 1,
      openExceptionCasesCount: 0,
    );
  }

  @override
  Future<OpenExceptionSummary> getOpenExceptions() async {
    return const OpenExceptionSummary(count: 0, cases: []);
  }

  @override
  Future<ReceiptSummary> cashCheckout({
    required String orderId,
    required int tenderedMinor,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<RefundSummary> refundPayment({
    required String paymentId,
    int? amountMinor,
    String? reason,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<VoidSummary> voidPayment({required String paymentId, String? reason}) {
    throw UnimplementedError();
  }

  @override
  Future<GiftCardSnapshot> lookupGiftCard(String code) {
    throw UnimplementedError();
  }

  @override
  Future<GiftCardSnapshot> issueGiftCard({
    required int amountMinor,
    String? customerId,
    String? requestedCode,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<GiftCardSnapshot> topUpGiftCard({
    required String giftCardCode,
    required int amountMinor,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<MembershipLookupSnapshot> lookupMembership({
    String? memberNumber,
    String? customerId,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<MembershipLookupSnapshot> activateMembership({
    required String customerId,
    required String membershipPlanId,
    String? memberNumber,
  }) {
    throw UnimplementedError();
  }

  @override
  Future<SyncRecoveryRunSnapshot> startSyncRecoveryRun() {
    throw UnimplementedError();
  }

  @override
  Future<SyncRecoveryRunSnapshot> getSyncRecoveryRun(String recoveryRunId) {
    throw UnimplementedError();
  }

  @override
  void close() {}
}

class _FakeCardTerminalGateway implements CardTerminalGateway {
  _FakeCardTerminalGateway(this._result);

  final TerminalCheckoutResult _result;

  @override
  Future<TerminalCheckoutResult> checkoutCard({
    required String orderId,
    required int amountMinor,
    required int tipMinor,
    String? terminalReference,
  }) async {
    return _result;
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
