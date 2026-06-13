import 'package:flutter/foundation.dart';

import '../core/hardware/barcode_scanner.dart';
import '../core/hardware/cash_drawer.dart';
import '../core/hardware/network_esc_pos_receipt_printer.dart';
import '../core/hardware/printer_endpoint_store.dart';
import '../core/services/device_security_state_store.dart';
import '../core/services/local_encryption_key_store.dart';
import '../core/services/local_data_protection_service.dart';
import '../core/services/debug_receipt_printer.dart';
import '../core/services/receipt_printer.dart';
import '../core/services/device_credentials_store.dart';
import '../core/services/device_identity_store.dart';
import '../core/services/pax_terminal_gateway.dart';
import '../data/local/pos_database.dart';
import '../data/remote/pos_api_client.dart';
import '../data/repositories/bootstrap_cache_repository.dart';
import '../data/repositories/receipt_print_queue_repository.dart';
import '../data/repositories/sync_outbox_repository.dart';
import '../features/home/pos_home_controller.dart';

class AppBootstrap {
  AppBootstrap({
    required this.database,
    required this.controller,
    required this.apiClient,
    required this.barcodeScanner,
  });

  final PosDatabase database;
  final PosHomeController controller;
  final PosApiClient apiClient;
  final BarcodeScanner barcodeScanner;

  Future<void> dispose() async {
    controller.dispose();
    barcodeScanner.dispose();
    apiClient.close();
    await database.close();
  }
}

class AppBootstrapper {
  static Future<AppBootstrap> bootstrap() async {
    final keyStore = LocalEncryptionKeyStore();
    final database = await PosDatabase.open(keyStore);
    final dataProtectionService = LocalDataProtectionService(
      keyStore: keyStore,
      database: database,
    );
    final securityStateStore = SecureDeviceSecurityStateStore();
    final terminalGateway = MethodChannelPaxTerminalGateway();

    await dataProtectionService.rotateIfDue();

    final bootstrapCacheRepository = BootstrapCacheRepository(database);
    final syncOutboxRepository = SyncOutboxRepository(database);
    final receiptPrintQueueRepository = ReceiptPrintQueueRepository(database);
    final credentialsStore = SecureDeviceCredentialsStore();
    final identityStore = DeviceIdentityStore();
    final apiClient = PosApiClient(
      credentialsStore: credentialsStore,
      identityStore: identityStore,
    );

    // Hardware adapters: real network/HID impls in release builds, debug stubs
    // in debug builds (and tests) so nothing requires physical hardware locally.
    final ReceiptPrinter receiptPrinter;
    final CashDrawer cashDrawer;
    final BarcodeScanner barcodeScanner;
    if (kDebugMode) {
      receiptPrinter = DebugReceiptPrinter();
      cashDrawer = DebugCashDrawer();
      barcodeScanner = DebugBarcodeScanner();
    } else {
      final endpointStore = PrinterEndpointStore();
      receiptPrinter = NetworkEscPosReceiptPrinter(endpointStore: endpointStore);
      cashDrawer = PrinterKickCashDrawer(endpointStore: endpointStore);
      barcodeScanner = HidKeyboardBarcodeScanner();
    }

    final controller = PosHomeController(
      bootstrapCacheRepository: bootstrapCacheRepository,
      syncOutboxRepository: syncOutboxRepository,
      receiptPrintQueueRepository: receiptPrintQueueRepository,
      credentialsStore: credentialsStore,
      securityStateStore: securityStateStore,
      gateway: apiClient,
      terminalGateway: terminalGateway,
      receiptPrinter: receiptPrinter,
      dataProtectionService: dataProtectionService,
      cashDrawer: cashDrawer,
    );

    await controller.load();

    return AppBootstrap(
      database: database,
      controller: controller,
      apiClient: apiClient,
      barcodeScanner: barcodeScanner,
    );
  }
}
