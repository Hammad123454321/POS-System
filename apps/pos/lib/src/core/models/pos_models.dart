import '../support/minor_amount.dart';

class DeviceCredentials {
  const DeviceCredentials({
    required this.apiBaseUrl,
    required this.deviceId,
    required this.accessToken,
    required this.accessTokenExpiresAt,
    required this.refreshToken,
    required this.refreshTokenExpiresAt,
    required this.tokenFamilyId,
    required this.silentRefreshWindowMinutes,
  });

  final String apiBaseUrl;
  final String deviceId;
  final String accessToken;
  final DateTime accessTokenExpiresAt;
  final String refreshToken;
  final DateTime refreshTokenExpiresAt;
  final String tokenFamilyId;
  final int silentRefreshWindowMinutes;

  bool shouldRefresh(DateTime nowUtc) {
    return accessTokenExpiresAt.isBefore(
      nowUtc.add(Duration(minutes: silentRefreshWindowMinutes)),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'apiBaseUrl': apiBaseUrl,
      'deviceId': deviceId,
      'accessToken': accessToken,
      'accessTokenExpiresAt': accessTokenExpiresAt.toIso8601String(),
      'refreshToken': refreshToken,
      'refreshTokenExpiresAt': refreshTokenExpiresAt.toIso8601String(),
      'tokenFamilyId': tokenFamilyId,
      'silentRefreshWindowMinutes': silentRefreshWindowMinutes,
    };
  }

  factory DeviceCredentials.fromJson(Map<String, dynamic> json) {
    return DeviceCredentials(
      apiBaseUrl: json['apiBaseUrl'] as String,
      deviceId: json['deviceId'] as String,
      accessToken: json['accessToken'] as String,
      accessTokenExpiresAt: DateTime.parse(
        json['accessTokenExpiresAt'] as String,
      ).toUtc(),
      refreshToken: json['refreshToken'] as String,
      refreshTokenExpiresAt: DateTime.parse(
        json['refreshTokenExpiresAt'] as String,
      ).toUtc(),
      tokenFamilyId: json['tokenFamilyId'] as String,
      silentRefreshWindowMinutes:
          json['silentRefreshWindowMinutes'] as int? ?? 5,
    );
  }

  DeviceCredentials copyWith({
    String? accessToken,
    DateTime? accessTokenExpiresAt,
    String? refreshToken,
    DateTime? refreshTokenExpiresAt,
    String? tokenFamilyId,
    int? silentRefreshWindowMinutes,
  }) {
    return DeviceCredentials(
      apiBaseUrl: apiBaseUrl,
      deviceId: deviceId,
      accessToken: accessToken ?? this.accessToken,
      accessTokenExpiresAt: accessTokenExpiresAt ?? this.accessTokenExpiresAt,
      refreshToken: refreshToken ?? this.refreshToken,
      refreshTokenExpiresAt:
          refreshTokenExpiresAt ?? this.refreshTokenExpiresAt,
      tokenFamilyId: tokenFamilyId ?? this.tokenFamilyId,
      silentRefreshWindowMinutes:
          silentRefreshWindowMinutes ?? this.silentRefreshWindowMinutes,
    );
  }
}

class DeviceIdentity {
  const DeviceIdentity({
    required this.publicKey,
    required this.deviceFingerprint,
    required this.attestation,
  });

  final String publicKey;
  final String deviceFingerprint;
  final Map<String, dynamic> attestation;
}

class TaxRuleSnapshot {
  const TaxRuleSnapshot({
    required this.id,
    required this.name,
    required this.rateBasisPoints,
    required this.isInclusive,
  });

  final String id;
  final String name;
  final int rateBasisPoints;
  final bool isInclusive;

  factory TaxRuleSnapshot.fromJson(Map<String, dynamic> json) {
    return TaxRuleSnapshot(
      id: json['id'] as String,
      name: json['name'] as String,
      rateBasisPoints: json['rate_basis_points'] as int? ?? 0,
      isInclusive: json['is_inclusive'] as bool? ?? false,
    );
  }
}

class CatalogItemSnapshot {
  const CatalogItemSnapshot({
    required this.id,
    required this.name,
    required this.currency,
    required this.effectivePriceMinor,
    required this.soldOut,
    this.taxRuleId,
  });

  final String id;
  final String name;
  final String currency;
  final int effectivePriceMinor;
  final bool soldOut;
  final String? taxRuleId;

  factory CatalogItemSnapshot.fromJson(Map<String, dynamic> json) {
    return CatalogItemSnapshot(
      id: json['id'] as String,
      name: json['name'] as String,
      currency: json['currency'] as String? ?? 'USD',
      effectivePriceMinor: json['effective_price_minor'] as int,
      soldOut: json['sold_out'] as bool? ?? false,
      taxRuleId: json['tax_rule_id'] as String?,
    );
  }
}

class DiscountRuleSnapshot {
  const DiscountRuleSnapshot({
    required this.id,
    required this.name,
    required this.type,
    this.code,
    this.valueMinor,
    this.valueBasisPoints,
  });

  final String id;
  final String name;
  final String type;
  final String? code;
  final int? valueMinor;
  final int? valueBasisPoints;

  factory DiscountRuleSnapshot.fromJson(Map<String, dynamic> json) {
    return DiscountRuleSnapshot(
      id: json['id'] as String,
      name: json['name'] as String,
      type: json['type'] as String,
      code: json['code'] as String?,
      valueMinor: json['value_minor'] as int?,
      valueBasisPoints: json['value_basis_points'] as int?,
    );
  }

  int applyToSubtotal(int subtotalMinor) {
    if (subtotalMinor <= 0) {
      return 0;
    }

    switch (type) {
      case 'fixed_minor':
        return (valueMinor ?? 0).clamp(0, subtotalMinor);
      case 'percent_basis_points':
        return MinorAmount.percentageOf(
          amountMinor: subtotalMinor,
          basisPoints: valueBasisPoints ?? 0,
        ).clamp(0, subtotalMinor);
      default:
        return 0;
    }
  }
}

class MembershipPlanSnapshot {
  const MembershipPlanSnapshot({
    required this.id,
    required this.name,
    required this.priceMinor,
    required this.currency,
    required this.durationDays,
    this.code,
  });

  final String id;
  final String name;
  final String? code;
  final int priceMinor;
  final String currency;
  final int durationDays;

  factory MembershipPlanSnapshot.fromJson(Map<String, dynamic> json) {
    return MembershipPlanSnapshot(
      id: json['id'] as String,
      name: json['name'] as String,
      code: json['code'] as String?,
      priceMinor: json['price_minor'] as int? ?? 0,
      currency: json['currency'] as String? ?? 'USD',
      durationDays: json['duration_days'] as int? ?? 0,
    );
  }
}

class PaymentCapabilitiesSnapshot {
  const PaymentCapabilitiesSnapshot({
    required this.defaultProvider,
    required this.supportedTenders,
    required this.terminalCapabilities,
  });

  final String defaultProvider;
  final List<String> supportedTenders;
  final Map<String, dynamic> terminalCapabilities;

  bool get supportsGiftCard => supportedTenders.contains('gift_card');

  factory PaymentCapabilitiesSnapshot.fromJson(Map<String, dynamic> json) {
    return PaymentCapabilitiesSnapshot(
      defaultProvider: json['default_provider'] as String? ?? '',
      supportedTenders:
          (json['supported_tenders'] as List<dynamic>? ?? const [])
              .map((item) => item as String)
              .toList(growable: false),
      terminalCapabilities: (json['terminal_capabilities'] as Map? ?? const {})
          .cast<String, dynamic>(),
    );
  }
}

class MemberAccountSnapshot {
  const MemberAccountSnapshot({
    required this.id,
    required this.memberNumber,
    required this.status,
  });

  final String id;
  final String memberNumber;
  final String status;

  factory MemberAccountSnapshot.fromJson(Map<String, dynamic> json) {
    return MemberAccountSnapshot(
      id: json['id'] as String,
      memberNumber: json['member_number'] as String,
      status: json['status'] as String,
    );
  }
}

class CustomerSummary {
  const CustomerSummary({
    required this.id,
    required this.name,
    this.phone,
    this.email,
    this.memberAccount,
  });

  final String id;
  final String name;
  final String? phone;
  final String? email;
  final MemberAccountSnapshot? memberAccount;

  factory CustomerSummary.fromJson(Map<String, dynamic> json) {
    final memberAccountJson = json['member_account'] as Map<String, dynamic>?;

    return CustomerSummary(
      id: json['id'] as String,
      name: json['name'] as String,
      phone: json['phone'] as String?,
      email: json['email'] as String?,
      memberAccount: memberAccountJson == null
          ? null
          : MemberAccountSnapshot.fromJson(memberAccountJson),
    );
  }
}

class RegisterSessionSnapshot {
  const RegisterSessionSnapshot({
    required this.id,
    required this.status,
    required this.expectedCashMinor,
    required this.sessionVersion,
    this.businessDate,
    this.openingFloatMinor,
    this.countedCashMinor,
    this.varianceMinor,
  });

  final String id;
  final String status;
  final int expectedCashMinor;
  final int sessionVersion;
  final String? businessDate;
  final int? openingFloatMinor;
  final int? countedCashMinor;
  final int? varianceMinor;

  factory RegisterSessionSnapshot.fromJson(Map<String, dynamic> json) {
    return RegisterSessionSnapshot(
      id: json['id'] as String,
      status: json['status'] as String,
      expectedCashMinor: json['expected_cash_minor'] as int? ?? 0,
      sessionVersion: json['session_version'] as int? ?? 1,
      businessDate: json['business_date'] as String?,
      openingFloatMinor: json['opening_float_minor'] as int?,
      countedCashMinor: json['counted_cash_minor'] as int?,
      varianceMinor: json['variance_minor'] as int?,
    );
  }
}

class PrinterTargetSnapshot {
  const PrinterTargetSnapshot({
    required this.id,
    required this.name,
    this.driverKey,
  });

  final String id;
  final String name;
  final String? driverKey;

  factory PrinterTargetSnapshot.fromJson(Map<String, dynamic> json) {
    return PrinterTargetSnapshot(
      id: json['id'] as String,
      name: json['name'] as String,
      driverKey: json['driver_key'] as String?,
    );
  }
}

class PrintRouteSnapshot {
  const PrintRouteSnapshot({
    required this.id,
    required this.routeKey,
    required this.documentType,
    required this.primaryPrinter,
    this.secondaryPrinter,
  });

  final String id;
  final String routeKey;
  final String documentType;
  final PrinterTargetSnapshot primaryPrinter;
  final PrinterTargetSnapshot? secondaryPrinter;

  factory PrintRouteSnapshot.fromJson(Map<String, dynamic> json) {
    final secondaryPrinterJson =
        json['secondary_printer'] as Map<String, dynamic>?;

    return PrintRouteSnapshot(
      id: json['id'] as String,
      routeKey: json['route_key'] as String,
      documentType: json['document_type'] as String? ?? 'receipt',
      primaryPrinter: PrinterTargetSnapshot.fromJson(
        (json['primary_printer'] as Map).cast<String, dynamic>(),
      ),
      secondaryPrinter: secondaryPrinterJson == null
          ? null
          : PrinterTargetSnapshot.fromJson(secondaryPrinterJson),
    );
  }

  List<String> get printerNames {
    return [
      primaryPrinter.name,
      if (secondaryPrinter != null) secondaryPrinter!.name,
    ];
  }
}

class DiningTableLeaseSnapshot {
  const DiningTableLeaseSnapshot({
    this.leaseVersion,
    this.currentHolderDeviceId,
    this.leaseExpiresAt,
    required this.isClaimedByCurrentDevice,
  });

  final int? leaseVersion;
  final String? currentHolderDeviceId;
  final DateTime? leaseExpiresAt;
  final bool isClaimedByCurrentDevice;

  factory DiningTableLeaseSnapshot.fromJson(Map<String, dynamic> json) {
    return DiningTableLeaseSnapshot(
      leaseVersion: json['lease_version'] as int?,
      currentHolderDeviceId: json['current_holder_device_id'] as String?,
      leaseExpiresAt: json['lease_expires_at'] == null
          ? null
          : DateTime.parse(json['lease_expires_at'] as String).toUtc(),
      isClaimedByCurrentDevice:
          json['is_claimed_by_current_device'] as bool? ?? false,
    );
  }
}

class DiningTableSnapshot {
  const DiningTableSnapshot({
    required this.id,
    required this.name,
    required this.capacity,
    required this.status,
    required this.lease,
    this.zoneName,
    this.currentPartyName,
    this.guestCount,
    this.assignedDeviceId,
  });

  final String id;
  final String name;
  final int capacity;
  final String status;
  final DiningTableLeaseSnapshot lease;
  final String? zoneName;
  final String? currentPartyName;
  final int? guestCount;
  final String? assignedDeviceId;

  factory DiningTableSnapshot.fromJson(Map<String, dynamic> json) {
    return DiningTableSnapshot(
      id: json['id'] as String,
      name: json['name'] as String,
      capacity: json['capacity'] as int? ?? 0,
      status: json['status'] as String? ?? 'available',
      lease: DiningTableLeaseSnapshot.fromJson(
        (json['lease'] as Map<String, dynamic>? ?? const {})
            .cast<String, dynamic>(),
      ),
      zoneName: json['zone_name'] as String?,
      currentPartyName: json['current_party_name'] as String?,
      guestCount: json['guest_count'] as int?,
      assignedDeviceId: json['assigned_device_id'] as String?,
    );
  }
}

class BusinessDaySummarySnapshot {
  const BusinessDaySummarySnapshot({
    required this.businessDate,
    required this.openOrdersCount,
    required this.paidOrdersCount,
    required this.grossSalesMinor,
    required this.cashSalesMinor,
    required this.openRegisterSessionsCount,
    required this.openExceptionCasesCount,
  });

  final String businessDate;
  final int openOrdersCount;
  final int paidOrdersCount;
  final int grossSalesMinor;
  final int cashSalesMinor;
  final int openRegisterSessionsCount;
  final int openExceptionCasesCount;

  factory BusinessDaySummarySnapshot.fromJson(Map<String, dynamic> json) {
    return BusinessDaySummarySnapshot(
      businessDate: json['business_date'] as String? ?? '',
      openOrdersCount: json['open_orders_count'] as int? ?? 0,
      paidOrdersCount: json['paid_orders_count'] as int? ?? 0,
      grossSalesMinor: json['gross_sales_minor'] as int? ?? 0,
      cashSalesMinor: json['cash_sales_minor'] as int? ?? 0,
      openRegisterSessionsCount:
          json['open_register_sessions_count'] as int? ?? 0,
      openExceptionCasesCount: json['open_exception_cases_count'] as int? ?? 0,
    );
  }
}

class ExceptionCaseSnapshot {
  const ExceptionCaseSnapshot({
    required this.id,
    required this.type,
    required this.severity,
    required this.message,
    this.recordType,
    this.recordId,
  });

  final String id;
  final String type;
  final String severity;
  final String message;
  final String? recordType;
  final String? recordId;

  factory ExceptionCaseSnapshot.fromJson(Map<String, dynamic> json) {
    return ExceptionCaseSnapshot(
      id: json['id'] as String,
      type: json['type'] as String? ?? '',
      severity: json['severity'] as String? ?? 'low',
      message: json['message'] as String? ?? '',
      recordType: json['record_type'] as String?,
      recordId: json['record_id'] as String?,
    );
  }
}

class OpenExceptionSummary {
  const OpenExceptionSummary({required this.count, required this.cases});

  final int count;
  final List<ExceptionCaseSnapshot> cases;

  factory OpenExceptionSummary.fromJson(Map<String, dynamic> json) {
    return OpenExceptionSummary(
      count: json['count'] as int? ?? 0,
      cases: ((json['cases'] as List<dynamic>? ?? const <dynamic>[]).map(
        (item) => ExceptionCaseSnapshot.fromJson(
          (item as Map).cast<String, dynamic>(),
        ),
      )).toList(growable: false),
    );
  }
}

class GiftCardSnapshot {
  const GiftCardSnapshot({
    required this.id,
    required this.code,
    required this.currency,
    required this.status,
    required this.currentBalanceMinor,
    this.issuedToCustomerId,
    this.labelPayload,
  });

  final String id;
  final String code;
  final String currency;
  final String status;
  final int currentBalanceMinor;
  final String? issuedToCustomerId;
  final Map<String, dynamic>? labelPayload;

  factory GiftCardSnapshot.fromJson(Map<String, dynamic> json) {
    return GiftCardSnapshot(
      id: json['id'] as String,
      code: json['code'] as String,
      currency: json['currency'] as String? ?? 'USD',
      status: json['status'] as String? ?? 'active',
      currentBalanceMinor: json['current_balance_minor'] as int? ?? 0,
      issuedToCustomerId: json['issued_to_customer_id'] as String?,
      labelPayload: (json['label_payload'] as Map?)?.cast<String, dynamic>(),
    );
  }
}

class MembershipLookupSnapshot {
  const MembershipLookupSnapshot({
    required this.memberAccountId,
    required this.memberNumber,
    required this.status,
    this.validUntil,
    this.customerId,
    this.customerName,
    this.membershipPlan,
  });

  final String memberAccountId;
  final String memberNumber;
  final String status;
  final String? validUntil;
  final String? customerId;
  final String? customerName;
  final MembershipPlanSnapshot? membershipPlan;

  factory MembershipLookupSnapshot.fromJson(Map<String, dynamic> json) {
    final customerJson = json['customer'] as Map<String, dynamic>?;
    final membershipPlanJson = json['membership_plan'] as Map<String, dynamic>?;

    return MembershipLookupSnapshot(
      memberAccountId: json['member_account_id'] as String,
      memberNumber: json['member_number'] as String,
      status: json['status'] as String? ?? 'active',
      validUntil: json['valid_until'] as String?,
      customerId: customerJson?['id'] as String?,
      customerName: customerJson?['name'] as String?,
      membershipPlan: membershipPlanJson == null
          ? null
          : MembershipPlanSnapshot.fromJson(membershipPlanJson),
    );
  }
}

class RefundSummary {
  const RefundSummary({
    required this.refundId,
    required this.paymentId,
    required this.status,
    required this.amountMinor,
    this.reason,
    this.refundedAt,
  });

  final String refundId;
  final String paymentId;
  final String status;
  final int amountMinor;
  final String? reason;
  final String? refundedAt;

  factory RefundSummary.fromJson(Map<String, dynamic> json) {
    return RefundSummary(
      refundId: json['refund_id'] as String,
      paymentId: json['payment_id'] as String,
      status: json['status'] as String? ?? '',
      amountMinor: json['amount_minor'] as int? ?? 0,
      reason: json['reason'] as String?,
      refundedAt: json['refunded_at'] as String?,
    );
  }
}

class VoidSummary {
  const VoidSummary({
    required this.voidRecordId,
    required this.paymentId,
    required this.status,
    this.reason,
    this.voidedAt,
  });

  final String voidRecordId;
  final String paymentId;
  final String status;
  final String? reason;
  final String? voidedAt;

  factory VoidSummary.fromJson(Map<String, dynamic> json) {
    return VoidSummary(
      voidRecordId: json['void_record_id'] as String,
      paymentId: json['payment_id'] as String,
      status: json['status'] as String? ?? '',
      reason: json['reason'] as String?,
      voidedAt: json['voided_at'] as String?,
    );
  }
}

class SyncRecoveryBatchSnapshot {
  const SyncRecoveryBatchSnapshot({
    required this.id,
    required this.name,
    required this.totalJobs,
    required this.pendingJobs,
    required this.failedJobs,
    required this.processedJobs,
    required this.progress,
    required this.cancelled,
    required this.finished,
  });

  final String id;
  final String name;
  final int totalJobs;
  final int pendingJobs;
  final int failedJobs;
  final int processedJobs;
  final int progress;
  final bool cancelled;
  final bool finished;

  factory SyncRecoveryBatchSnapshot.fromJson(Map<String, dynamic> json) {
    return SyncRecoveryBatchSnapshot(
      id: json['id'] as String,
      name: json['name'] as String? ?? '',
      totalJobs: json['total_jobs'] as int? ?? 0,
      pendingJobs: json['pending_jobs'] as int? ?? 0,
      failedJobs: json['failed_jobs'] as int? ?? 0,
      processedJobs: json['processed_jobs'] as int? ?? 0,
      progress: json['progress'] as int? ?? 0,
      cancelled: json['cancelled'] as bool? ?? false,
      finished: json['finished'] as bool? ?? false,
    );
  }
}

class SyncRecoveryRunSnapshot {
  const SyncRecoveryRunSnapshot({
    required this.id,
    required this.status,
    required this.eventCount,
    this.batchId,
    this.startedAt,
    this.finishedAt,
    this.batch,
  });

  final String id;
  final String status;
  final int eventCount;
  final String? batchId;
  final String? startedAt;
  final String? finishedAt;
  final SyncRecoveryBatchSnapshot? batch;

  factory SyncRecoveryRunSnapshot.fromJson(Map<String, dynamic> json) {
    final batchJson = json['batch'] as Map<String, dynamic>?;

    return SyncRecoveryRunSnapshot(
      id: json['id'] as String,
      status: json['status'] as String? ?? '',
      eventCount: json['event_count'] as int? ?? 0,
      batchId: json['batch_id'] as String?,
      startedAt: json['started_at'] as String?,
      finishedAt: json['finished_at'] as String?,
      batch: batchJson == null
          ? null
          : SyncRecoveryBatchSnapshot.fromJson(batchJson),
    );
  }
}

class TenderPaymentSnapshot {
  const TenderPaymentSnapshot({
    required this.paymentId,
    required this.method,
    required this.status,
    required this.amountMinor,
    required this.appliedMinor,
    required this.tipMinor,
    required this.tenderedMinor,
    required this.changeMinor,
    this.providerKey,
    this.providerTransactionId,
    this.terminalReference,
    this.maskedPan,
    this.authCode,
    this.giftCardCode,
  });

  final String paymentId;
  final String method;
  final String status;
  final int amountMinor;
  final int appliedMinor;
  final int tipMinor;
  final int tenderedMinor;
  final int changeMinor;
  final String? providerKey;
  final String? providerTransactionId;
  final String? terminalReference;
  final String? maskedPan;
  final String? authCode;
  final String? giftCardCode;

  factory TenderPaymentSnapshot.fromJson(Map<String, dynamic> json) {
    return TenderPaymentSnapshot(
      paymentId: json['payment_id'] as String,
      method: json['method'] as String? ?? '',
      status: json['status'] as String? ?? '',
      amountMinor: json['amount_minor'] as int? ?? 0,
      appliedMinor: json['applied_minor'] as int? ?? 0,
      tipMinor: json['tip_minor'] as int? ?? 0,
      tenderedMinor: json['tendered_minor'] as int? ?? 0,
      changeMinor: json['change_minor'] as int? ?? 0,
      providerKey: json['provider_key'] as String?,
      providerTransactionId: json['provider_transaction_id'] as String?,
      terminalReference: json['terminal_reference'] as String?,
      maskedPan: json['masked_pan'] as String?,
      authCode: json['auth_code'] as String?,
      giftCardCode: json['gift_card_code'] as String?,
    );
  }
}

class ReceiptSummary {
  const ReceiptSummary({
    required this.receiptId,
    required this.receiptNumber,
    required this.payload,
  });

  final String receiptId;
  final String receiptNumber;
  final Map<String, dynamic> payload;

  List<TenderPaymentSnapshot> get payments {
    return ((payload['payments'] as List<dynamic>? ?? const <dynamic>[]).map(
      (item) =>
          TenderPaymentSnapshot.fromJson((item as Map).cast<String, dynamic>()),
    )).toList(growable: false);
  }
}

class OrderSummary {
  const OrderSummary({
    required this.id,
    required this.orderNumber,
    required this.totalMinor,
    this.customerId,
    this.discountMinor,
  });

  final String id;
  final String orderNumber;
  final int totalMinor;
  final String? customerId;
  final int? discountMinor;
}

class CartLineSnapshot {
  const CartLineSnapshot({required this.item, required this.quantity});

  final CatalogItemSnapshot item;
  final int quantity;

  int get lineSubtotalMinor => item.effectivePriceMinor * quantity;
}

class WorkforceStaffSnapshot {
  const WorkforceStaffSnapshot({
    required this.id,
    required this.displayName,
    this.roleTitle,
  });

  final String id;
  final String displayName;
  final String? roleTitle;

  factory WorkforceStaffSnapshot.fromJson(Map<String, dynamic> json) {
    return WorkforceStaffSnapshot(
      id: json['id'] as String,
      displayName:
          json['display_name'] as String? ?? json['name'] as String? ?? '',
      roleTitle: json['role_title'] as String?,
    );
  }
}

class AppointmentSnapshot {
  const AppointmentSnapshot({
    required this.id,
    required this.status,
    required this.startsAt,
    required this.endsAt,
    this.staffProfileId,
    this.serviceItemId,
    this.customerId,
    this.customerName,
  });

  final String id;
  final String status;
  final String startsAt;
  final String endsAt;
  final String? staffProfileId;
  final String? serviceItemId;
  final String? customerId;
  final String? customerName;

  factory AppointmentSnapshot.fromJson(Map<String, dynamic> json) {
    return AppointmentSnapshot(
      id: json['id'] as String,
      status: json['status'] as String? ?? '',
      startsAt: json['starts_at'] as String? ?? '',
      endsAt: json['ends_at'] as String? ?? '',
      staffProfileId: json['staff_profile_id'] as String?,
      serviceItemId: json['service_item_id'] as String?,
      customerId: json['customer_id'] as String?,
      customerName: json['customer_name'] as String?,
    );
  }
}

class ShiftSnapshot {
  const ShiftSnapshot({
    required this.id,
    required this.status,
    this.staffProfileId,
    this.openedAt,
    this.closedAt,
  });

  final String id;
  final String status;
  final String? staffProfileId;
  final String? openedAt;
  final String? closedAt;

  factory ShiftSnapshot.fromJson(Map<String, dynamic> json) {
    return ShiftSnapshot(
      id: json['id'] as String,
      status: json['status'] as String? ?? '',
      staffProfileId: json['staff_profile_id'] as String?,
      openedAt: json['opened_at'] as String?,
      closedAt: json['closed_at'] as String?,
    );
  }
}

class DeliveryOrderSnapshot {
  const DeliveryOrderSnapshot({
    required this.id,
    required this.channelKey,
    required this.externalOrderId,
    required this.status,
    this.orderId,
  });

  final String id;
  final String channelKey;
  final String externalOrderId;
  final String status;
  final String? orderId;

  factory DeliveryOrderSnapshot.fromJson(Map<String, dynamic> json) {
    return DeliveryOrderSnapshot(
      id: json['id'] as String,
      channelKey: json['channel_key'] as String? ?? '',
      externalOrderId: json['external_order_id'] as String? ?? '',
      status: json['status'] as String? ?? '',
      orderId: json['order_id'] as String?,
    );
  }
}

class RetailOperationSnapshot {
  const RetailOperationSnapshot({required this.payload});

  final Map<String, dynamic> payload;

  factory RetailOperationSnapshot.fromJson(Map<String, dynamic> json) {
    return RetailOperationSnapshot(payload: json);
  }
}
