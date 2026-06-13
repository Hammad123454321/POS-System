import 'package:drift/drift.dart' show Value;
import 'package:flutter_test/flutter_test.dart';
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

void main() {
  late PosDatabase database;
  late PosHomeController controller;

  setUp(() async {
    database = PosDatabase.memory();
    await _seedBootstrap(database);
    controller = PosHomeController(
      bootstrapCacheRepository: BootstrapCacheRepository(database),
      syncOutboxRepository: SyncOutboxRepository(database),
      receiptPrintQueueRepository: ReceiptPrintQueueRepository(database),
      credentialsStore: _FakeCredsStore(),
      securityStateStore: _FakeSecurityStore(),
      gateway: _NoopGateway(),
      terminalGateway: _NoopTerminalGateway(),
      receiptPrinter: _NoopReceiptPrinter(),
      dataProtectionService: _NoopDataProtection(),
    );
    await controller.load();
  });

  tearDown(() async {
    controller.dispose();
    await database.close();
  });

  test('setItemQuantity sets absolute quantity and quantityOf reflects it', () {
    final item = controller.catalogItems.first;
    controller.setItemQuantity(item, 3);
    expect(controller.quantityOf(item.id), 3);

    controller.setItemQuantity(item, 0);
    expect(controller.quantityOf(item.id), 0);
  });

  test('removeLine clears the whole line regardless of quantity', () {
    final item = controller.catalogItems.first;
    controller.addItem(item);
    controller.addItem(item);
    expect(controller.quantityOf(item.id), 2);

    controller.removeLine(item);
    expect(controller.quantityOf(item.id), 0);
    expect(controller.cartLines, isEmpty);
  });

  test('clearCart empties all lines', () {
    controller.addItem(controller.catalogItems.first);
    expect(controller.cartLines, isNotEmpty);

    controller.clearCart();
    expect(controller.cartLines, isEmpty);
  });

  test('catalogCategories returns distinct sorted category names', () {
    expect(controller.catalogCategories, ['Mains']);
  });

  test('addItemByScanCode matches by SKU (case-insensitive) and adds', () {
    expect(controller.addItemByScanCode('brg'), isTrue);
    expect(controller.quantityOf('item-1'), 1);
  });

  test('addItemByScanCode matches by item id', () {
    expect(controller.addItemByScanCode('item-1'), isTrue);
    expect(controller.quantityOf('item-1'), 1);
  });

  test('addItemByScanCode returns false when nothing matches', () {
    expect(controller.addItemByScanCode('GIFT-9999'), isFalse);
    expect(controller.cartLines, isEmpty);
  });

  test('noSaleOpenDrawer without an open register surfaces an error', () async {
    await controller.noSaleOpenDrawer();
    expect(controller.errorMessage, contains('Open a register session'));
  });
}

Future<void> _seedBootstrap(PosDatabase database) {
  return database.into(database.deviceBootstrapCaches).insert(
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
    "register": {"active_session": null}
  },
  "config": {
    "items": [
      {"id": "item-1", "name": "Burger", "currency": "USD", "effective_price_minor": 1200, "sold_out": false, "category_id": "cat-1", "category_name": "Mains", "sku": "BRG"}
    ],
    "tax_rules": [], "discount_rules": [], "membership_plans": [],
    "payment_capabilities": {"default_provider": "fiserv_bluepay", "supported_tenders": ["cash"], "terminal_capabilities": {}},
    "print_routes": []
  },
  "tables": {"tables": []},
  "business_day_summary": {"business_date": "2026-04-24", "open_orders_count": 0, "paid_orders_count": 0, "gross_sales_minor": 0, "cash_sales_minor": 0, "open_register_sessions_count": 0, "open_exception_cases_count": 0},
  "exceptions": {"count": 0, "cases": []},
  "deltas": {"cursor": "2026-04-24T12:00:00Z"}
}
''',
          syncedAt: DateTime(2026, 4, 24, 12),
        ),
      );
}

class _FakeCredsStore implements DeviceCredentialsStore {
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

class _FakeSecurityStore implements DeviceSecurityStateStore {
  @override
  Future<bool> requiresReauth() async => false;

  @override
  Future<void> setRequiresReauth(bool value) async {}
}

class _NoopGateway implements PosGateway {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

class _NoopTerminalGateway implements CardTerminalGateway {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

class _NoopReceiptPrinter implements ReceiptPrinter {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

class _NoopDataProtection implements LocalDataProtectionService {
  @override
  noSuchMethod(Invocation invocation) => throw UnimplementedError();
}
