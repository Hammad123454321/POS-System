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

  Future<List<WorkforceStaffSnapshot>> listWorkforceStaff();
  Future<List<AppointmentSnapshot>> listAppointments({
    String? startDate,
    String? endDate,
  });
  Future<Map<String, dynamic>> claimAppointmentSlot({
    required String staffProfileId,
    required String startsAt,
    required String endsAt,
  });
  Future<AppointmentSnapshot> createAppointment({
    required String slotClaimId,
    required String staffProfileId,
    required String serviceItemId,
    required String startsAt,
    required String endsAt,
    String? customerId,
    String source,
    int discountMinor,
    String? notes,
  });
  Future<AppointmentSnapshot> checkInAppointment(String appointmentId);
  Future<AppointmentSnapshot> completeAppointment(String appointmentId);
  Future<ShiftSnapshot> openShift({
    required String staffProfileId,
    int? openingCashMinor,
  });
  Future<ShiftSnapshot> closeShift({
    required String shiftId,
    int? closingCashMinor,
    String? notes,
  });
  Future<Map<String, dynamic>> getLaborAnalytics({
    String? startDate,
    String? endDate,
  });

  Future<List<DeliveryOrderSnapshot>> listExternalDeliveryOrders();
  Future<DeliveryOrderSnapshot> ingestExternalDeliveryOrder({
    required String channelKey,
    required String externalOrderId,
    String? externalStoreId,
    List<Map<String, dynamic>> lines,
    Map<String, dynamic> payload,
  });
  Future<DeliveryOrderSnapshot> confirmExternalDeliveryOrder(String linkId);
  Future<DeliveryOrderSnapshot> updateExternalDeliveryOrderStatus({
    required String linkId,
    required String status,
  });
  Future<DeliveryOrderSnapshot> cancelExternalDeliveryOrder({
    required String linkId,
    String? reason,
  });
  Future<Map<String, dynamic>> setDeliveryStoreAvailability({
    required bool isAvailable,
    String? reason,
  });
  Future<Map<String, dynamic>> setDeliveryItemAvailability({
    required String catalogItemId,
    required bool isAvailable,
    String? reason,
  });

  Future<RetailOperationSnapshot> lookupRetailInventory({
    String? sku,
    String? barcode,
  });
  Future<RetailOperationSnapshot> receiveRetailStock({
    required String documentNumber,
    required List<Map<String, dynamic>> lines,
    String? supplierName,
    String? reason,
  });
  Future<RetailOperationSnapshot> transferRetailStock({
    required String destinationStoreId,
    required String documentNumber,
    required List<Map<String, dynamic>> lines,
    String? reason,
  });
  Future<RetailOperationSnapshot> adjustRetailStock({
    required String sku,
    required int quantityDelta,
    required String reason,
    String? documentNumber,
  });
  Future<RetailOperationSnapshot> processRetailReturn({
    required String documentNumber,
    required List<Map<String, dynamic>> lines,
    String? reason,
  });

  void close();
}
