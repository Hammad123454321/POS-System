import '../../core/models/pos_models.dart';
import '../repositories/sync_outbox_repository.dart';

class PosApiException implements Exception {
  const PosApiException({
    required this.message,
    required this.statusCode,
    this.errorCode,
    this.requiresReauth = false,
  });

  final String message;
  final int statusCode;
  final String? errorCode;
  final bool requiresReauth;
}

abstract interface class PosGateway {
  Future<DeviceCredentials> enroll({
    required String apiBaseUrl,
    required String enrollmentCode,
    required String deviceName,
  });

  Future<Map<String, dynamic>> bootstrap();
  Future<Map<String, dynamic>> config();
  Future<Map<String, dynamic>> pullSyncDeltas(String? cursor);
  Future<List<Map<String, dynamic>>> pushSyncEvents(
    List<SyncOutboxEntryData> events,
  );

  Future<RegisterSessionSnapshot> openRegisterSession(int openingFloatMinor);
  Future<RegisterSessionSnapshot> closeRegisterSession({
    required String registerSessionId,
    required int countedCashMinor,
    required int sessionVersion,
  });

  Future<List<CustomerSummary>> searchCustomers(String query);
  Future<List<DiningTableSnapshot>> listDiningTables();
  Future<DiningTableSnapshot> claimDiningTable({
    required String diningTableId,
    String? currentPartyName,
    int? guestCount,
  });
  Future<DiningTableSnapshot> heartbeatDiningTable(String diningTableId);
  Future<DiningTableSnapshot> releaseDiningTable(String diningTableId);
  Future<BusinessDaySummarySnapshot> getBusinessDaySummary();
  Future<OpenExceptionSummary> getOpenExceptions();

  Future<OrderSummary> createOrder({
    required String registerSessionId,
    required List<Map<String, dynamic>> lines,
    String? customerId,
    String? discountRuleId,
  });

  Future<ReceiptSummary> tenderOrder({
    required String orderId,
    required List<Map<String, dynamic>> tenders,
  });

  Future<ReceiptSummary> cashCheckout({
    required String orderId,
    required int tenderedMinor,
  });

  Future<RefundSummary> refundPayment({
    required String paymentId,
    int? amountMinor,
    String? reason,
  });

  Future<VoidSummary> voidPayment({required String paymentId, String? reason});

  Future<GiftCardSnapshot> lookupGiftCard(String code);
  Future<GiftCardSnapshot> issueGiftCard({
    required int amountMinor,
    String? customerId,
    String? requestedCode,
  });
  Future<GiftCardSnapshot> topUpGiftCard({
    required String giftCardCode,
    required int amountMinor,
  });

  Future<MembershipLookupSnapshot> lookupMembership({
    String? memberNumber,
    String? customerId,
  });

  Future<MembershipLookupSnapshot> activateMembership({
    required String customerId,
    required String membershipPlanId,
    String? memberNumber,
  });

  Future<SyncRecoveryRunSnapshot> startSyncRecoveryRun();
  Future<SyncRecoveryRunSnapshot> getSyncRecoveryRun(String recoveryRunId);

  void close();
}
