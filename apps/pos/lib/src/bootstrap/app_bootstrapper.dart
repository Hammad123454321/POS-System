import '../core/services/device_security_state_store.dart';
import '../core/services/local_encryption_key_store.dart';
import '../core/services/local_data_protection_service.dart';
import '../core/services/debug_receipt_printer.dart';
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
  });

  final PosDatabase database;
  final PosHomeController controller;
  final PosApiClient apiClient;

  Future<void> dispose() async {
    controller.dispose();
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

    final controller = PosHomeController(
      bootstrapCacheRepository: bootstrapCacheRepository,
      syncOutboxRepository: syncOutboxRepository,
      receiptPrintQueueRepository: receiptPrintQueueRepository,
      credentialsStore: credentialsStore,
      securityStateStore: securityStateStore,
      gateway: apiClient,
      terminalGateway: terminalGateway,
      receiptPrinter: DebugReceiptPrinter(),
      dataProtectionService: dataProtectionService,
    );

    await controller.load();

    return AppBootstrap(
      database: database,
      controller: controller,
      apiClient: apiClient,
    );
  }
}
