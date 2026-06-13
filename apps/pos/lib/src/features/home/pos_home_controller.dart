import 'dart:async';
import 'dart:math';

import 'package:flutter/foundation.dart';

import '../../core/models/pos_models.dart';
import '../../core/services/device_credentials_store.dart';
import '../../core/services/pax_terminal_gateway.dart';
import '../../core/services/device_security_state_store.dart';
import '../../core/services/local_data_protection_service.dart';
import '../../core/services/receipt_printer.dart';
import '../../core/support/minor_amount.dart';
import '../../data/remote/pos_gateway.dart';
import '../../data/repositories/bootstrap_cache_repository.dart';
import '../../data/repositories/receipt_print_queue_repository.dart';
import '../../data/repositories/sync_outbox_repository.dart';

class PosHomeController extends ChangeNotifier {
  PosHomeController({
    required BootstrapCacheRepository bootstrapCacheRepository,
    required SyncOutboxRepository syncOutboxRepository,
    required ReceiptPrintQueueRepository receiptPrintQueueRepository,
    required DeviceCredentialsStore credentialsStore,
    required DeviceSecurityStateStore securityStateStore,
    required PosGateway gateway,
    required CardTerminalGateway terminalGateway,
    required ReceiptPrinter receiptPrinter,
    required LocalDataProtectionService dataProtectionService,
  }) : _bootstrapCacheRepository = bootstrapCacheRepository,
       _syncOutboxRepository = syncOutboxRepository,
       _receiptPrintQueueRepository = receiptPrintQueueRepository,
       _credentialsStore = credentialsStore,
       _securityStateStore = securityStateStore,
       _gateway = gateway,
       _terminalGateway = terminalGateway,
       _receiptPrinter = receiptPrinter,
       _dataProtectionService = dataProtectionService;

  final BootstrapCacheRepository _bootstrapCacheRepository;
  final SyncOutboxRepository _syncOutboxRepository;
  final ReceiptPrintQueueRepository _receiptPrintQueueRepository;
  final DeviceCredentialsStore _credentialsStore;
  final DeviceSecurityStateStore _securityStateStore;
  final PosGateway _gateway;
  final CardTerminalGateway _terminalGateway;
  final ReceiptPrinter _receiptPrinter;
  final LocalDataProtectionService _dataProtectionService;

  final Map<String, int> _cart = <String, int>{};

  bool _isLoading = true;
  bool _isBusy = false;
  bool _requiresReauth = false;
  String? _errorMessage;
  String? _statusMessage;
  DeviceCredentials? _credentials;
  BootstrapCacheSnapshot? _bootstrapSnapshot;
  RegisterSessionSnapshot? _activeRegisterSession;
  ReceiptSummary? _lastReceipt;
  List<CatalogItemSnapshot> _catalogItems = const [];
  List<TaxRuleSnapshot> _taxRules = const [];
  List<DiscountRuleSnapshot> _discountRules = const [];
  List<MembershipPlanSnapshot> _membershipPlans = const [];
  List<PrintRouteSnapshot> _printRoutes = const [];
  List<DiningTableSnapshot> _diningTables = const [];
  List<WorkforceStaffSnapshot> _workforceStaff = const [];
  List<AppointmentSnapshot> _appointments = const [];
  List<DeliveryOrderSnapshot> _deliveryOrders = const [];
  BusinessDaySummarySnapshot? _businessDaySummary;
  OpenExceptionSummary? _openExceptions;
  Map<String, dynamic>? _laborAnalytics;
  RetailOperationSnapshot? _lastRetailOperation;
  List<CustomerSummary> _customerResults = const [];
  CustomerSummary? _selectedCustomer;
  DiscountRuleSnapshot? _selectedDiscount;
  MembershipPlanSnapshot? _selectedMembershipPlan;
  PaymentCapabilitiesSnapshot? _paymentCapabilities;
  GiftCardSnapshot? _giftCardSnapshot;
  MembershipLookupSnapshot? _membershipLookup;
  SyncRecoveryRunSnapshot? _lastSyncRecoveryRun;
  String? _inDoubtOrderId;
  int _pendingSyncEvents = 0;
  int _authHoldSyncEvents = 0;
  int _pendingPrintJobs = 0;
  Timer? _tableLeaseHeartbeatTimer;
  String? _heartbeatTableId;

  bool get isLoading => _isLoading;
  bool get isBusy => _isBusy;
  bool get isEnrolled => _credentials != null;
  bool get requiresReauth => _requiresReauth;
  String? get errorMessage => _errorMessage;
  String? get statusMessage => _statusMessage;
  BootstrapCacheSnapshot? get bootstrapSnapshot => _bootstrapSnapshot;
  RegisterSessionSnapshot? get activeRegisterSession => _activeRegisterSession;
  ReceiptSummary? get lastReceipt => _lastReceipt;
  int get pendingSyncEvents => _pendingSyncEvents;
  int get authHoldSyncEvents => _authHoldSyncEvents;
  int get pendingPrintJobs => _pendingPrintJobs;
  List<CatalogItemSnapshot> get catalogItems => _catalogItems;
  List<DiscountRuleSnapshot> get discountRules => _discountRules;
  List<MembershipPlanSnapshot> get membershipPlans => _membershipPlans;
  List<PrintRouteSnapshot> get printRoutes => _printRoutes;
  List<DiningTableSnapshot> get diningTables => _diningTables;
  List<WorkforceStaffSnapshot> get workforceStaff => _workforceStaff;
  List<AppointmentSnapshot> get appointments => _appointments;
  List<DeliveryOrderSnapshot> get deliveryOrders => _deliveryOrders;
  BusinessDaySummarySnapshot? get businessDaySummary => _businessDaySummary;
  OpenExceptionSummary? get openExceptions => _openExceptions;
  Map<String, dynamic>? get laborAnalytics => _laborAnalytics;
  RetailOperationSnapshot? get lastRetailOperation => _lastRetailOperation;
  List<CustomerSummary> get customerResults => _customerResults;
  CustomerSummary? get selectedCustomer => _selectedCustomer;
  DiscountRuleSnapshot? get selectedDiscount => _selectedDiscount;
  MembershipPlanSnapshot? get selectedMembershipPlan => _selectedMembershipPlan;
  PaymentCapabilitiesSnapshot? get paymentCapabilities => _paymentCapabilities;
  GiftCardSnapshot? get giftCardSnapshot => _giftCardSnapshot;
  MembershipLookupSnapshot? get membershipLookup => _membershipLookup;
  SyncRecoveryRunSnapshot? get lastSyncRecoveryRun => _lastSyncRecoveryRun;
  String? get inDoubtOrderId => _inDoubtOrderId;

  DiningTableSnapshot? get activeClaimedTable {
    for (final table in _diningTables) {
      if (table.lease.isClaimedByCurrentDevice) {
        return table;
      }
    }

    return null;
  }

  PrintRouteSnapshot? get defaultReceiptRoute {
    for (final route in _printRoutes) {
      if (route.routeKey == 'receipt-default') {
        return route;
      }
    }

    for (final route in _printRoutes) {
      if (route.documentType == 'receipt') {
        return route;
      }
    }

    return null;
  }

  PrintRouteSnapshot? get defaultLabelRoute {
    for (final route in _printRoutes) {
      if (route.routeKey == 'label-default') {
        return route;
      }
    }

    for (final route in _printRoutes) {
      if (route.documentType == 'label') {
        return route;
      }
    }

    return null;
  }

  List<CartLineSnapshot> get cartLines {
    return _catalogItems
        .where((item) => _cart.containsKey(item.id))
        .map(
          (item) => CartLineSnapshot(item: item, quantity: _cart[item.id] ?? 0),
        )
        .where((line) => line.quantity > 0)
        .toList(growable: false);
  }

  int get cartEstimatedSubtotalMinor {
    return cartLines.fold<int>(
      0,
      (total, line) => total + line.lineSubtotalMinor,
    );
  }

  int get cartEstimatedDiscountMinor {
    return _selectedDiscount?.applyToSubtotal(cartEstimatedSubtotalMinor) ?? 0;
  }

  int get cartEstimatedTaxMinor {
    final lineSubtotals = cartLines
        .map((line) => line.lineSubtotalMinor)
        .toList();
    final allocatedDiscounts = MinorAmount.allocateAcross(
      lineSubtotals,
      cartEstimatedDiscountMinor,
    );
    var taxMinor = 0;

    for (var index = 0; index < cartLines.length; index += 1) {
      final line = cartLines[index];
      final discountedSubtotal =
          line.lineSubtotalMinor - allocatedDiscounts[index];
      final taxRule = _taxRuleForItem(line.item);

      if (taxRule == null) {
        continue;
      }

      taxMinor += MinorAmount.calculateTax(
        netMinor: discountedSubtotal,
        rateBasisPoints: taxRule.rateBasisPoints,
        inclusive: taxRule.isInclusive,
      );
    }

    return taxMinor;
  }

  int get cartEstimatedTotalMinor {
    return cartEstimatedSubtotalMinor -
        cartEstimatedDiscountMinor +
        cartEstimatedTaxMinor;
  }

  Future<void> load() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    _credentials = await _credentialsStore.load();
    _requiresReauth = await _securityStateStore.requiresReauth();
    _bootstrapSnapshot = await _bootstrapCacheRepository.latest();
    await _refreshCounts();
    _hydrateFromCache();

    _isLoading = false;
    notifyListeners();
  }

  Future<void> enroll({
    required String apiBaseUrl,
    required String enrollmentCode,
    required String deviceName,
  }) async {
    await _runBusy(() async {
      final hadReauthLock = _requiresReauth;
      _credentials = await _gateway.enroll(
        apiBaseUrl: apiBaseUrl.trim(),
        enrollmentCode: enrollmentCode.trim(),
        deviceName: deviceName.trim(),
      );

      if (hadReauthLock || _authHoldSyncEvents > 0) {
        await _dataProtectionService.rotateForTrigger('device_reenrollment');
      }

      await _securityStateStore.setRequiresReauth(false);
      _requiresReauth = false;
      _statusMessage = _authHoldSyncEvents > 0
          ? 'Device re-enrolled. Held sync events remain quarantined until manager approval.'
          : 'Device enrolled. Cloud bootstrap is now available.';
      await _refreshCloudStateInternal();
    });
  }

  Future<void> refreshCloudState() async {
    await _runBusy(() async {
      await _refreshCloudStateInternal();
      _statusMessage =
          'Cloud bootstrap, catalog, tables, reports, and deltas refreshed.';
    });
  }

  Future<void> syncNow() async {
    await _runBusy(() async {
      final pending = await _syncOutboxRepository.pending();
      final accepted = await _gateway.pushSyncEvents(pending);

      await _syncOutboxRepository.markSynced(
        accepted
            .map((item) => item['local_event_id'] as String)
            .toList(growable: false),
      );

      await _refreshCloudStateInternal();
      _statusMessage = _authHoldSyncEvents > 0
          ? 'Pending device sync completed. Held sync events remain quarantined.'
          : 'Device sync completed.';
    });
  }

  Future<void> approveHeldSyncEvents() async {
    if (_requiresReauth) {
      _errorMessage =
          'Re-enroll this device before approving held sync events.';
      notifyListeners();
      return;
    }

    if (_authHoldSyncEvents == 0) {
      _statusMessage = 'No held sync events require approval.';
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      await _syncOutboxRepository.releaseAuthHoldToPending();
      await _refreshCounts();
      _statusMessage =
          'Held sync events were approved and returned to the pending queue.';
    });
  }

  Future<void> openRegister(int openingFloatMinor) async {
    await _runBusy(() async {
      final session = await _gateway.openRegisterSession(openingFloatMinor);
      _activeRegisterSession = session;

      await _syncOutboxRepository.enqueue(
        localEventId: _localEventId(),
        entityType: 'register_session',
        action: 'opened',
        payload: {
          'register_session_id': session.id,
          'opening_float_minor': openingFloatMinor,
        },
      );

      await _refreshCounts();
      await _refreshCloudStateInternal();
      _statusMessage = 'Register session opened.';
    });
  }

  Future<void> closeRegister(int countedCashMinor) async {
    final session = _activeRegisterSession;

    if (session == null) {
      _errorMessage = 'Open a register session before trying to close it.';
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      final closed = await _gateway.closeRegisterSession(
        registerSessionId: session.id,
        countedCashMinor: countedCashMinor,
        sessionVersion: session.sessionVersion,
      );

      await _syncOutboxRepository.enqueue(
        localEventId: _localEventId(),
        entityType: 'register_session',
        action: 'closed',
        payload: {
          'register_session_id': closed.id,
          'counted_cash_minor': countedCashMinor,
          'variance_minor': closed.varianceMinor,
          'session_version': closed.sessionVersion,
        },
      );

      _activeRegisterSession = null;
      await _refreshCounts();
      await _refreshCloudStateInternal();
      _statusMessage = 'Register session closed.';
    });
  }

  Future<void> searchCustomers(String query) async {
    final normalized = query.trim();

    if (normalized.isEmpty) {
      _customerResults = const [];
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      _customerResults = await _gateway.searchCustomers(normalized);
      _statusMessage = _customerResults.isEmpty
          ? 'No customers matched "$normalized".'
          : 'Customer search refreshed.';
    });
  }

  void selectCustomer(String? customerId) {
    if (customerId == null || customerId.isEmpty) {
      _selectedCustomer = null;
      notifyListeners();
      return;
    }

    for (final customer in _customerResults) {
      if (customer.id == customerId) {
        _selectedCustomer = customer;
        notifyListeners();
        return;
      }
    }
  }

  void selectDiscount(String? discountId) {
    if (discountId == null || discountId.isEmpty) {
      _selectedDiscount = null;
      notifyListeners();
      return;
    }

    for (final discount in _discountRules) {
      if (discount.id == discountId) {
        _selectedDiscount = discount;
        notifyListeners();
        return;
      }
    }
  }

  void selectMembershipPlan(String? membershipPlanId) {
    if (membershipPlanId == null || membershipPlanId.isEmpty) {
      _selectedMembershipPlan = null;
      notifyListeners();
      return;
    }

    for (final plan in _membershipPlans) {
      if (plan.id == membershipPlanId) {
        _selectedMembershipPlan = plan;
        notifyListeners();
        return;
      }
    }
  }

  Future<void> claimDiningTable({
    required String diningTableId,
    String? currentPartyName,
    int? guestCount,
  }) async {
    await _runBusy(() async {
      final updated = await _gateway.claimDiningTable(
        diningTableId: diningTableId,
        currentPartyName: currentPartyName?.trim(),
        guestCount: guestCount,
      );

      _replaceDiningTable(updated);
      await _refreshOperationalReads();
      _statusMessage = 'Dining table ${updated.name} claimed on this device.';
    });
  }

  Future<void> releaseDiningTable(String diningTableId) async {
    await _runBusy(() async {
      final updated = await _gateway.releaseDiningTable(diningTableId);
      _replaceDiningTable(updated);
      await _refreshOperationalReads();
      _statusMessage = 'Dining table ${updated.name} released.';
    });
  }

  Future<void> checkoutCash(int tenderedMinor) async {
    if (tenderedMinor <= 0) {
      _errorMessage = 'Enter the tendered cash amount before checkout.';
      notifyListeners();
      return;
    }

    await _checkoutOrder(
      successMessage: 'Cash checkout completed and receipt queued.',
      buildTenders: (order) async => [
        {
          'method': 'cash',
          'amount_minor': order.totalMinor,
          'tendered_minor': tenderedMinor,
        },
      ],
    );
  }

  Future<void> checkoutCard(int tipMinor) async {
    if (_inDoubtOrderId != null) {
      _errorMessage =
          'Card recovery is required for order $_inDoubtOrderId before retrying card tender.';
      notifyListeners();
      return;
    }

    OrderSummary? inDoubtOrder;
    TerminalCheckoutResult? inDoubtResult;

    await _checkoutOrder(
      successMessage: 'Card checkout completed and receipt queued.',
      onInDoubt: () {
        final order = inDoubtOrder;
        final terminalResult = inDoubtResult;

        if (order == null || terminalResult == null) {
          return;
        }

        _inDoubtOrderId = order.id;
        _statusMessage =
            'Card terminal did not return a final result. Order ${order.orderNumber} is locked for manager recovery.';
        unawaited(
          _syncOutboxRepository.enqueue(
            localEventId: _localEventId(),
            entityType: 'payment',
            action: 'card_in_doubt',
            payload: {
              'order_id': order.id,
              'order_number': order.orderNumber,
              'provider_key': terminalResult.providerKey,
              'provider_transaction_id': terminalResult.providerTransactionId,
              'terminal_id': terminalResult.terminalId,
              'terminal_status_code': terminalResult.terminalStatusCode,
              'terminal_result_code': terminalResult.terminalResultCode,
            },
          ),
        );
      },
      buildTenders: (order) async {
        final terminalResult = await _terminalGateway.checkoutCard(
          orderId: order.id,
          amountMinor: order.totalMinor,
          tipMinor: tipMinor,
        );

        if (terminalResult.isInDoubt) {
          inDoubtOrder = order;
          inDoubtResult = terminalResult;
          return const [];
        }

        if (!terminalResult.isApproved) {
          throw StateError(
            terminalResult.message ?? 'Card terminal declined the transaction.',
          );
        }

        return [
          {
            'method': 'card',
            'amount_minor': order.totalMinor,
            if (tipMinor > 0) 'tip_minor': tipMinor,
            'provider_key': terminalResult.providerKey,
            'provider_transaction_id': terminalResult.providerTransactionId,
            'auth_code': terminalResult.authCode,
            'masked_pan': terminalResult.maskedPan,
            'terminal_id': terminalResult.terminalId,
            'entry_mode': terminalResult.entryMode,
            'application_label': terminalResult.applicationLabel,
            'aid': terminalResult.aid,
            'tvr': terminalResult.tvr,
            'tsi': terminalResult.tsi,
            'terminal_status_code': terminalResult.terminalStatusCode,
            'terminal_result_code': terminalResult.terminalResultCode,
            'terminal_timestamp': terminalResult.terminalTimestamp
                .toIso8601String(),
            if (terminalResult.terminalReference != null)
              'terminal_reference': terminalResult.terminalReference,
          },
        ];
      },
    );
  }

  Future<void> checkoutSplitCashCard({
    required int cashAppliedMinor,
    required int cashTenderedMinor,
    required int cardTipMinor,
  }) async {
    if (_inDoubtOrderId != null) {
      _errorMessage =
          'Card recovery is required for order $_inDoubtOrderId before retrying split tender.';
      notifyListeners();
      return;
    }

    OrderSummary? inDoubtOrder;
    TerminalCheckoutResult? inDoubtResult;

    await _checkoutOrder(
      successMessage: 'Split tender checkout completed and receipt queued.',
      onInDoubt: () {
        final order = inDoubtOrder;
        final terminalResult = inDoubtResult;

        if (order == null || terminalResult == null) {
          return;
        }

        _inDoubtOrderId = order.id;
        _statusMessage =
            'Split tender card leg is in-doubt for order ${order.orderNumber}. Manager recovery is required.';
        unawaited(
          _syncOutboxRepository.enqueue(
            localEventId: _localEventId(),
            entityType: 'payment',
            action: 'card_in_doubt',
            payload: {
              'order_id': order.id,
              'order_number': order.orderNumber,
              'provider_key': terminalResult.providerKey,
              'provider_transaction_id': terminalResult.providerTransactionId,
              'terminal_id': terminalResult.terminalId,
              'terminal_status_code': terminalResult.terminalStatusCode,
              'terminal_result_code': terminalResult.terminalResultCode,
            },
          ),
        );
      },
      buildTenders: (order) async {
        if (cashAppliedMinor <= 0 || cashAppliedMinor >= order.totalMinor) {
          throw StateError(
            'Split cash applied amount must be between zero and the order total.',
          );
        }

        final cardAppliedMinor = order.totalMinor - cashAppliedMinor;
        final terminalResult = await _terminalGateway.checkoutCard(
          orderId: order.id,
          amountMinor: cardAppliedMinor,
          tipMinor: cardTipMinor,
        );

        if (terminalResult.isInDoubt) {
          inDoubtOrder = order;
          inDoubtResult = terminalResult;
          return const [];
        }

        if (!terminalResult.isApproved) {
          throw StateError(
            terminalResult.message ??
                'Card terminal declined the split tender card leg.',
          );
        }

        return [
          {
            'method': 'cash',
            'amount_minor': cashAppliedMinor,
            'tendered_minor': cashTenderedMinor,
          },
          {
            'method': 'card',
            'amount_minor': cardAppliedMinor,
            if (cardTipMinor > 0) 'tip_minor': cardTipMinor,
            'provider_key': terminalResult.providerKey,
            'provider_transaction_id': terminalResult.providerTransactionId,
            'auth_code': terminalResult.authCode,
            'masked_pan': terminalResult.maskedPan,
            'terminal_id': terminalResult.terminalId,
            'entry_mode': terminalResult.entryMode,
            'application_label': terminalResult.applicationLabel,
            'aid': terminalResult.aid,
            'tvr': terminalResult.tvr,
            'tsi': terminalResult.tsi,
            'terminal_status_code': terminalResult.terminalStatusCode,
            'terminal_result_code': terminalResult.terminalResultCode,
            'terminal_timestamp': terminalResult.terminalTimestamp
                .toIso8601String(),
            if (terminalResult.terminalReference != null)
              'terminal_reference': terminalResult.terminalReference,
          },
        ];
      },
    );
  }

  Future<void> checkoutGiftCard(String giftCardCode) async {
    final normalizedCode = giftCardCode.trim();

    if (normalizedCode.isEmpty) {
      _errorMessage = 'Enter a gift card code before tendering stored value.';
      notifyListeners();
      return;
    }

    await _checkoutOrder(
      successMessage: 'Gift card checkout completed and receipt queued.',
      buildTenders: (order) async => [
        {
          'method': 'gift_card',
          'amount_minor': order.totalMinor,
          'gift_card_code': normalizedCode,
        },
      ],
    );
  }

  Future<void> lookupGiftCard(String code) async {
    final normalizedCode = code.trim();

    if (normalizedCode.isEmpty) {
      _giftCardSnapshot = null;
      _errorMessage = 'Enter a gift card code to run an online lookup.';
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      try {
        _giftCardSnapshot = await _gateway.lookupGiftCard(normalizedCode);
        _statusMessage = 'Gift card balance refreshed from the cloud.';
      } on PosApiException catch (exception) {
        if (exception.statusCode == 404) {
          _giftCardSnapshot = null;
          _statusMessage = 'No active gift card matched "$normalizedCode".';
          return;
        }

        rethrow;
      }
    });
  }

  Future<void> issueGiftCard({
    required int amountMinor,
    String? requestedCode,
  }) async {
    if (amountMinor <= 0) {
      _errorMessage = 'Enter a positive amount before issuing a gift card.';
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      final giftCard = await _gateway.issueGiftCard(
        amountMinor: amountMinor,
        customerId: _selectedCustomer?.id,
        requestedCode: requestedCode?.trim().isEmpty ?? true
            ? null
            : requestedCode?.trim(),
      );

      _giftCardSnapshot = giftCard;

      await _queueDocumentPrint(
        documentId: giftCard.id,
        routeKey: defaultLabelRoute?.routeKey ?? 'label-default',
        payload: {
          'route_key': defaultLabelRoute?.routeKey ?? 'label-default',
          'printer_names': defaultLabelRoute?.printerNames ?? const <String>[],
          ...?giftCard.labelPayload,
        },
      );

      await _printPendingReceipts();
      await _refreshCounts();
      _statusMessage = 'Gift card issued and label queued for printing.';
    });
  }

  Future<void> topUpGiftCard({
    required String giftCardCode,
    required int amountMinor,
  }) async {
    final normalizedCode = giftCardCode.trim();

    if (normalizedCode.isEmpty || amountMinor <= 0) {
      _errorMessage =
          'Enter a gift card code and a positive amount before topping up.';
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      _giftCardSnapshot = await _gateway.topUpGiftCard(
        giftCardCode: normalizedCode,
        amountMinor: amountMinor,
      );
      _statusMessage = 'Gift card top-up completed.';
    });
  }

  Future<void> lookupMembership({
    String? memberNumber,
    String? customerId,
  }) async {
    final normalizedMemberNumber = memberNumber?.trim();
    final normalizedCustomerId = customerId?.trim();

    if ((normalizedMemberNumber?.isEmpty ?? true) &&
        (normalizedCustomerId?.isEmpty ?? true)) {
      _membershipLookup = null;
      _errorMessage =
          'Select a customer or enter a member number before lookup.';
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      try {
        _membershipLookup = await _gateway.lookupMembership(
          memberNumber: normalizedMemberNumber,
          customerId: normalizedCustomerId,
        );
        _statusMessage = 'Membership lookup refreshed from the cloud.';
      } on PosApiException catch (exception) {
        if (exception.statusCode == 404) {
          _membershipLookup = null;
          _statusMessage = 'No active membership matched the requested lookup.';
          return;
        }

        rethrow;
      }
    });
  }

  Future<void> activateMembership({String? memberNumber}) async {
    final selectedCustomer = _selectedCustomer;
    final selectedMembershipPlan = _selectedMembershipPlan;

    if (selectedCustomer == null || selectedMembershipPlan == null) {
      _errorMessage =
          'Select both a customer and membership plan before activation.';
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      _membershipLookup = await _gateway.activateMembership(
        customerId: selectedCustomer.id,
        membershipPlanId: selectedMembershipPlan.id,
        memberNumber: memberNumber?.trim().isEmpty ?? true
            ? null
            : memberNumber?.trim(),
      );
      _statusMessage = 'Membership activated for ${selectedCustomer.name}.';
    });
  }

  Future<void> refundPayment({
    required String paymentId,
    int? amountMinor,
    String? reason,
  }) async {
    await _runBusy(() async {
      final refund = await _gateway.refundPayment(
        paymentId: paymentId,
        amountMinor: amountMinor,
        reason: reason?.trim(),
      );

      await _syncOutboxRepository.enqueue(
        localEventId: _localEventId(),
        entityType: 'payment',
        action: 'refunded',
        payload: {
          'payment_id': paymentId,
          'refund_id': refund.refundId,
          'amount_minor': refund.amountMinor,
          'status': refund.status,
        },
      );

      _patchLastReceiptPaymentStatus(paymentId, refund.status);
      await _refreshCounts();
      await _refreshCloudStateInternal();
      _statusMessage = 'Payment refund submitted.';
    });
  }

  Future<void> voidPayment({required String paymentId, String? reason}) async {
    await _runBusy(() async {
      final voidSummary = await _gateway.voidPayment(
        paymentId: paymentId,
        reason: reason?.trim(),
      );

      await _syncOutboxRepository.enqueue(
        localEventId: _localEventId(),
        entityType: 'payment',
        action: 'voided',
        payload: {
          'payment_id': paymentId,
          'void_record_id': voidSummary.voidRecordId,
          'status': voidSummary.status,
        },
      );

      _patchLastReceiptPaymentStatus(paymentId, voidSummary.status);
      await _refreshCounts();
      await _refreshCloudStateInternal();
      _statusMessage = 'Payment void submitted.';
    });
  }

  Future<void> startSyncRecovery() async {
    await _runBusy(() async {
      var run = await _gateway.startSyncRecoveryRun();

      if (run.batchId != null && !(run.batch?.finished ?? false)) {
        run = await _gateway.getSyncRecoveryRun(run.id);
      }

      _lastSyncRecoveryRun = run;
      await _refreshCloudStateInternal();
      _statusMessage = 'Sync recovery run submitted.';
    });
  }

  Future<void> refreshPhaseThreeFourOperations() async {
    await _runBusy(() async {
      await _refreshOperationalReads();
      _statusMessage =
          'Workforce, delivery, and retail operation state refreshed.';
    });
  }

  Future<void> checkInAppointment(String appointmentId) async {
    await _runBusy(() async {
      await _gateway.checkInAppointment(appointmentId);
      await _refreshOperationalReads();
      _statusMessage = 'Appointment checked in.';
    });
  }

  Future<void> completeAppointment(String appointmentId) async {
    await _runBusy(() async {
      await _gateway.completeAppointment(appointmentId);
      await _refreshOperationalReads();
      _statusMessage = 'Appointment completed.';
    });
  }

  Future<void> openStaffShift({
    required String staffProfileId,
    int? openingCashMinor,
  }) async {
    await _runBusy(() async {
      await _gateway.openShift(
        staffProfileId: staffProfileId,
        openingCashMinor: openingCashMinor,
      );
      await _refreshOperationalReads();
      _statusMessage = 'Staff shift opened.';
    });
  }

  Future<void> closeStaffShift({
    required String shiftId,
    int? closingCashMinor,
    String? notes,
  }) async {
    await _runBusy(() async {
      await _gateway.closeShift(
        shiftId: shiftId,
        closingCashMinor: closingCashMinor,
        notes: notes?.trim().isEmpty ?? true ? null : notes!.trim(),
      );
      await _refreshOperationalReads();
      _statusMessage = 'Staff shift closed.';
    });
  }

  Future<void> setStoreDeliveryAvailability(bool isAvailable) async {
    await _runBusy(() async {
      await _gateway.setDeliveryStoreAvailability(
        isAvailable: isAvailable,
        reason: isAvailable
            ? 'POS resumed store availability.'
            : 'POS paused store availability.',
      );
      await _refreshOperationalReads();
      _statusMessage = isAvailable
          ? 'Delivery store availability resumed.'
          : 'Delivery store availability paused.';
    });
  }

  Future<void> confirmDeliveryOrder(String linkId) async {
    await _runBusy(() async {
      await _gateway.confirmExternalDeliveryOrder(linkId);
      await _refreshOperationalReads();
      _statusMessage = 'Delivery order confirmed.';
    });
  }

  Future<void> updateDeliveryOrderStatus({
    required String linkId,
    required String status,
  }) async {
    await _runBusy(() async {
      await _gateway.updateExternalDeliveryOrderStatus(
        linkId: linkId,
        status: status,
      );
      await _refreshOperationalReads();
      _statusMessage = 'Delivery order status updated.';
    });
  }

  Future<void> lookupRetailInventory({String? sku, String? barcode}) async {
    await _runBusy(() async {
      _lastRetailOperation = await _gateway.lookupRetailInventory(
        sku: sku,
        barcode: barcode,
      );
      _statusMessage = 'Retail inventory lookup completed.';
    });
  }

  Future<void> receiveRetailStock({
    required String documentNumber,
    required String sku,
    required int quantity,
    String? reason,
  }) async {
    await _runBusy(() async {
      _lastRetailOperation = await _gateway.receiveRetailStock(
        documentNumber: documentNumber,
        reason: reason?.trim().isEmpty ?? true ? null : reason!.trim(),
        lines: [
          {'sku': sku, 'quantity': quantity},
        ],
      );
      await _refreshOperationalReads();
      _statusMessage = 'Retail stock receipt submitted.';
    });
  }

  Future<void> transferRetailStock({
    required String destinationStoreId,
    required String documentNumber,
    required String sku,
    required int quantity,
    String? reason,
  }) async {
    await _runBusy(() async {
      _lastRetailOperation = await _gateway.transferRetailStock(
        destinationStoreId: destinationStoreId,
        documentNumber: documentNumber,
        reason: reason?.trim().isEmpty ?? true ? null : reason!.trim(),
        lines: [
          {'sku': sku, 'quantity': quantity},
        ],
      );
      await _refreshOperationalReads();
      _statusMessage = 'Retail stock transfer submitted.';
    });
  }

  Future<void> adjustRetailStock({
    required String sku,
    required int quantityDelta,
    required String reason,
  }) async {
    await _runBusy(() async {
      _lastRetailOperation = await _gateway.adjustRetailStock(
        sku: sku,
        quantityDelta: quantityDelta,
        reason: reason,
      );
      await _refreshOperationalReads();
      _statusMessage = 'Retail stock adjustment submitted.';
    });
  }

  Future<void> processRetailReturn({
    required String documentNumber,
    required String sku,
    required int quantity,
    String? reason,
  }) async {
    await _runBusy(() async {
      _lastRetailOperation = await _gateway.processRetailReturn(
        documentNumber: documentNumber,
        reason: reason?.trim().isEmpty ?? true ? null : reason!.trim(),
        lines: [
          {'sku': sku, 'quantity': quantity},
        ],
      );
      await _refreshOperationalReads();
      _statusMessage = 'Retail return submitted.';
    });
  }

  Future<void> printPendingReceipts() async {
    await _runBusy(() async {
      await _printPendingReceipts();
      await _refreshCounts();
      _statusMessage = 'Receipt spool processed.';
    });
  }

  void addItem(CatalogItemSnapshot item) {
    if (item.soldOut) {
      _errorMessage = '${item.name} is marked sold out.';
      notifyListeners();
      return;
    }

    _cart.update(item.id, (value) => value + 1, ifAbsent: () => 1);
    _errorMessage = null;
    notifyListeners();
  }

  void removeItem(CatalogItemSnapshot item) {
    final current = _cart[item.id];

    if (current == null) {
      return;
    }

    if (current <= 1) {
      _cart.remove(item.id);
    } else {
      _cart[item.id] = current - 1;
    }

    notifyListeners();
  }

  /// Remove an entire cart line regardless of its quantity.
  void removeLine(CatalogItemSnapshot item) {
    if (_cart.remove(item.id) != null) {
      notifyListeners();
    }
  }

  /// Set the absolute quantity for an item (0 removes it).
  void setItemQuantity(CatalogItemSnapshot item, int quantity) {
    if (quantity <= 0) {
      _cart.remove(item.id);
    } else {
      _cart[item.id] = quantity;
    }
    notifyListeners();
  }

  /// Current quantity of an item id in the cart.
  int quantityOf(String itemId) => _cart[itemId] ?? 0;

  /// Clear all cart lines.
  void clearCart() {
    if (_cart.isEmpty) {
      return;
    }
    _cart.clear();
    notifyListeners();
  }

  /// Distinct, sorted category names present in the loaded catalog.
  List<String> get catalogCategories {
    final names = <String>{};
    for (final item in _catalogItems) {
      final name = item.categoryName;
      if (name != null && name.isNotEmpty) {
        names.add(name);
      }
    }
    final sorted = names.toList()..sort();
    return sorted;
  }

  @override
  void dispose() {
    _tableLeaseHeartbeatTimer?.cancel();
    super.dispose();
  }

  Future<void> _printPendingReceipts() async {
    final jobs = await _receiptPrintQueueRepository.pending();

    for (final job in jobs) {
      final route = _routeForKey(job.routeKey);

      await _receiptPrinter.printReceipt(
        receiptId: job.receiptId ?? job.id,
        routeKey: job.routeKey,
        printerNames: route?.printerNames ?? const <String>[],
        payload: job.payload,
      );

      await _receiptPrintQueueRepository.markCompleted(job.id);
    }
  }

  Future<void> _refreshCounts() async {
    _pendingSyncEvents = await _syncOutboxRepository.pendingCount();
    _authHoldSyncEvents = await _syncOutboxRepository.authHoldCount();
    _pendingPrintJobs = await _receiptPrintQueueRepository.pendingCount();
  }

  void _hydrateFromCache() {
    final payload = _bootstrapSnapshot?.payload;

    if (payload == null || payload.isEmpty) {
      _catalogItems = const [];
      _taxRules = const [];
      _discountRules = const [];
      _membershipPlans = const [];
      _printRoutes = const [];
      _paymentCapabilities = null;
      _giftCardSnapshot = null;
      _membershipLookup = null;
      _lastSyncRecoveryRun = null;
      _diningTables = const [];
      _workforceStaff = const [];
      _appointments = const [];
      _deliveryOrders = const [];
      _businessDaySummary = null;
      _openExceptions = null;
      _laborAnalytics = null;
      _lastRetailOperation = null;
      _activeRegisterSession = null;
      _syncTableLeaseHeartbeat();
      return;
    }

    final config = _asMap(payload['config']);
    final bootstrap = _asMap(payload['bootstrap']);
    final tablesPayload = _asMap(payload['tables']);
    final workforcePayload = _asMap(payload['workforce']);
    final deliveryPayload = _asMap(payload['delivery']);
    final businessDaySummaryPayload = _asMap(payload['business_day_summary']);
    final exceptionsPayload = _asMap(payload['exceptions']);

    _catalogItems =
        ((config['items'] as List<dynamic>? ?? const <dynamic>[]).map(
          (item) => CatalogItemSnapshot.fromJson(
            (item as Map).cast<String, dynamic>(),
          ),
        )).toList(growable: false);
    _taxRules =
        ((config['tax_rules'] as List<dynamic>? ?? const <dynamic>[]).map(
          (item) =>
              TaxRuleSnapshot.fromJson((item as Map).cast<String, dynamic>()),
        )).toList(growable: false);
    _discountRules =
        ((config['discount_rules'] as List<dynamic>? ?? const <dynamic>[]).map(
          (item) => DiscountRuleSnapshot.fromJson(
            (item as Map).cast<String, dynamic>(),
          ),
        )).toList(growable: false);
    _membershipPlans =
        ((config['membership_plans'] as List<dynamic>? ?? const <dynamic>[])
                .map(
                  (item) => MembershipPlanSnapshot.fromJson(
                    (item as Map).cast<String, dynamic>(),
                  ),
                ))
            .toList(growable: false);
    _printRoutes =
        ((config['print_routes'] as List<dynamic>? ?? const <dynamic>[]).map(
          (item) => PrintRouteSnapshot.fromJson(
            (item as Map).cast<String, dynamic>(),
          ),
        )).toList(growable: false);
    final paymentCapabilities = _asMap(config['payment_capabilities']);
    _paymentCapabilities = paymentCapabilities.isEmpty
        ? null
        : PaymentCapabilitiesSnapshot.fromJson(paymentCapabilities);

    final register = _asMap(bootstrap['register']);
    final active = _asMap(register['active_session']);
    _activeRegisterSession = active.isEmpty
        ? null
        : RegisterSessionSnapshot.fromJson(active);

    _diningTables =
        ((tablesPayload['tables'] as List<dynamic>? ?? const <dynamic>[]).map(
          (item) => DiningTableSnapshot.fromJson(
            (item as Map).cast<String, dynamic>(),
          ),
        )).toList(growable: false);
    _workforceStaff =
        ((workforcePayload['staff'] as List<dynamic>? ?? const <dynamic>[]).map(
          (item) => WorkforceStaffSnapshot.fromJson(
            (item as Map).cast<String, dynamic>(),
          ),
        )).toList(growable: false);
    _appointments =
        ((workforcePayload['appointments'] as List<dynamic>? ??
                    const <dynamic>[])
                .map(
                  (item) => AppointmentSnapshot.fromJson(
                    (item as Map).cast<String, dynamic>(),
                  ),
                ))
            .toList(growable: false);
    _laborAnalytics = _asMap(workforcePayload['labor_analytics']);
    _deliveryOrders =
        ((deliveryPayload['orders'] as List<dynamic>? ?? const <dynamic>[]).map(
          (item) => DeliveryOrderSnapshot.fromJson(
            (item as Map).cast<String, dynamic>(),
          ),
        )).toList(growable: false);
    _businessDaySummary = businessDaySummaryPayload.isEmpty
        ? null
        : BusinessDaySummarySnapshot.fromJson(businessDaySummaryPayload);
    _openExceptions = exceptionsPayload.isEmpty
        ? null
        : OpenExceptionSummary.fromJson(exceptionsPayload);

    if (_selectedDiscount != null) {
      _selectedDiscount = _discountRules.firstWhere(
        (discount) => discount.id == _selectedDiscount!.id,
        orElse: () => _selectedDiscount!,
      );

      if (!_discountRules.any(
        (discount) => discount.id == _selectedDiscount!.id,
      )) {
        _selectedDiscount = null;
      }
    }

    if (_selectedMembershipPlan != null) {
      _selectedMembershipPlan = _membershipPlans.firstWhere(
        (plan) => plan.id == _selectedMembershipPlan!.id,
        orElse: () => _selectedMembershipPlan!,
      );

      if (!_membershipPlans.any(
        (plan) => plan.id == _selectedMembershipPlan!.id,
      )) {
        _selectedMembershipPlan = null;
      }
    }

    _syncTableLeaseHeartbeat();
  }

  Future<void> _refreshCloudStateInternal() async {
    final bootstrap = await _gateway.bootstrap();
    final config = await _gateway.config();
    final cursor =
        (_asMap(_bootstrapSnapshot?.payload['deltas'])['cursor'] as String?);
    final deltas = await _gateway.pullSyncDeltas(cursor);
    final tables = await _gateway.listDiningTables();
    final staff = await _gateway.listWorkforceStaff();
    final appointments = await _gateway.listAppointments();
    final laborAnalytics = await _gateway.getLaborAnalytics();
    final deliveryOrders = await _gateway.listExternalDeliveryOrders();
    final businessDaySummary = await _gateway.getBusinessDaySummary();
    final exceptions = await _gateway.getOpenExceptions();

    await _persistSnapshot(
      bootstrap: bootstrap,
      config: config,
      deltas: deltas,
      tables: {'tables': tables.map(_tableToJson).toList(growable: false)},
      workforce: {
        'staff': staff.map(_staffToJson).toList(growable: false),
        'appointments': appointments
            .map(_appointmentToJson)
            .toList(growable: false),
        'labor_analytics': laborAnalytics,
      },
      delivery: {
        'orders': deliveryOrders
            .map(_deliveryOrderToJson)
            .toList(growable: false),
      },
      businessDaySummary: _businessDaySummaryToJson(businessDaySummary),
      exceptions: _openExceptionsToJson(exceptions),
    );
  }

  Future<void> _refreshOperationalReads() async {
    final tables = await _gateway.listDiningTables();
    final staff = await _gateway.listWorkforceStaff();
    final appointments = await _gateway.listAppointments();
    final laborAnalytics = await _gateway.getLaborAnalytics();
    final deliveryOrders = await _gateway.listExternalDeliveryOrders();
    final businessDaySummary = await _gateway.getBusinessDaySummary();
    final exceptions = await _gateway.getOpenExceptions();

    _diningTables = tables;
    _workforceStaff = staff;
    _appointments = appointments;
    _laborAnalytics = laborAnalytics;
    _deliveryOrders = deliveryOrders;
    _businessDaySummary = businessDaySummary;
    _openExceptions = exceptions;
    _syncTableLeaseHeartbeat();

    await _persistSnapshot(
      tables: {'tables': tables.map(_tableToJson).toList(growable: false)},
      workforce: {
        'staff': staff.map(_staffToJson).toList(growable: false),
        'appointments': appointments
            .map(_appointmentToJson)
            .toList(growable: false),
        'labor_analytics': laborAnalytics,
      },
      delivery: {
        'orders': deliveryOrders
            .map(_deliveryOrderToJson)
            .toList(growable: false),
      },
      businessDaySummary: _businessDaySummaryToJson(businessDaySummary),
      exceptions: _openExceptionsToJson(exceptions),
    );
  }

  Future<void> _persistSnapshot({
    Map<String, dynamic>? bootstrap,
    Map<String, dynamic>? config,
    Map<String, dynamic>? deltas,
    Map<String, dynamic>? tables,
    Map<String, dynamic>? workforce,
    Map<String, dynamic>? delivery,
    Map<String, dynamic>? businessDaySummary,
    Map<String, dynamic>? exceptions,
  }) async {
    final currentPayload =
        _bootstrapSnapshot?.payload ?? const <String, dynamic>{};
    final effectiveBootstrap = bootstrap ?? _asMap(currentPayload['bootstrap']);
    final effectiveConfig = config ?? _asMap(currentPayload['config']);
    final effectiveDeltas = deltas ?? _asMap(currentPayload['deltas']);
    final effectiveTables = tables ?? _asMap(currentPayload['tables']);
    final effectiveWorkforce = workforce ?? _asMap(currentPayload['workforce']);
    final effectiveDelivery = delivery ?? _asMap(currentPayload['delivery']);
    final effectiveSummary =
        businessDaySummary ?? _asMap(currentPayload['business_day_summary']);
    final effectiveExceptions =
        exceptions ?? _asMap(currentPayload['exceptions']);
    final merchant = _asMap(effectiveBootstrap['merchant']);
    final store = _asMap(effectiveBootstrap['store']);
    final device = _asMap(effectiveBootstrap['device']);

    await _bootstrapCacheRepository.replace(
      apiBaseUrl:
          _credentials?.apiBaseUrl ?? _bootstrapSnapshot?.apiBaseUrl ?? '',
      deviceId: device['id'] as String? ?? _bootstrapSnapshot?.deviceId ?? '',
      merchantId:
          merchant['id'] as String? ?? _bootstrapSnapshot?.merchantId ?? '',
      storeId: store['id'] as String? ?? _bootstrapSnapshot?.storeId ?? '',
      payload: {
        'bootstrap': effectiveBootstrap,
        'config': effectiveConfig,
        'deltas': effectiveDeltas,
        'tables': effectiveTables,
        'workforce': effectiveWorkforce,
        'delivery': effectiveDelivery,
        'business_day_summary': effectiveSummary,
        'exceptions': effectiveExceptions,
      },
    );

    _bootstrapSnapshot = await _bootstrapCacheRepository.latest();
    _hydrateFromCache();
  }

  Future<void> _heartbeatActiveTableLease(String tableId) async {
    try {
      final updated = await _gateway.heartbeatDiningTable(tableId);
      _replaceDiningTable(updated, syncHeartbeat: false);
      notifyListeners();
    } on PosApiException catch (exception) {
      if (exception.requiresReauth) {
        await _enterAuthHoldState();
        notifyListeners();
        return;
      }

      if (exception.errorCode == 'LEASE_EXPIRED' ||
          exception.errorCode == 'LEASE_CONFLICT') {
        _errorMessage = 'Dining table control expired on this device.';
        _tableLeaseHeartbeatTimer?.cancel();
        _tableLeaseHeartbeatTimer = null;
        _heartbeatTableId = null;
        await _refreshOperationalReads();
        notifyListeners();
      }
    }
  }

  void _replaceDiningTable(
    DiningTableSnapshot updated, {
    bool syncHeartbeat = true,
  }) {
    final next = [..._diningTables];
    final index = next.indexWhere((table) => table.id == updated.id);

    if (index == -1) {
      next.add(updated);
    } else {
      next[index] = updated;
    }

    _diningTables = next;

    if (syncHeartbeat) {
      _syncTableLeaseHeartbeat();
    }
  }

  void _syncTableLeaseHeartbeat() {
    final table = activeClaimedTable;

    if (table == null) {
      _heartbeatTableId = null;
      _tableLeaseHeartbeatTimer?.cancel();
      _tableLeaseHeartbeatTimer = null;
      return;
    }

    if (_heartbeatTableId == table.id &&
        _tableLeaseHeartbeatTimer?.isActive == true) {
      return;
    }

    _heartbeatTableId = table.id;
    _tableLeaseHeartbeatTimer?.cancel();
    _tableLeaseHeartbeatTimer = Timer.periodic(const Duration(seconds: 10), (
      _,
    ) {
      unawaited(_heartbeatActiveTableLease(table.id));
    });
  }

  TaxRuleSnapshot? _taxRuleForItem(CatalogItemSnapshot item) {
    if (item.taxRuleId == null) {
      return null;
    }

    for (final taxRule in _taxRules) {
      if (taxRule.id == item.taxRuleId) {
        return taxRule;
      }
    }

    return null;
  }

  PrintRouteSnapshot? _routeForKey(String routeKey) {
    for (final route in _printRoutes) {
      if (route.routeKey == routeKey) {
        return route;
      }
    }

    return null;
  }

  Map<String, dynamic> _tableToJson(DiningTableSnapshot table) {
    return {
      'id': table.id,
      'name': table.name,
      'zone_name': table.zoneName,
      'capacity': table.capacity,
      'status': table.status,
      'current_party_name': table.currentPartyName,
      'guest_count': table.guestCount,
      'assigned_device_id': table.assignedDeviceId,
      'lease': {
        'lease_version': table.lease.leaseVersion,
        'current_holder_device_id': table.lease.currentHolderDeviceId,
        'lease_expires_at': table.lease.leaseExpiresAt?.toIso8601String(),
        'is_claimed_by_current_device': table.lease.isClaimedByCurrentDevice,
      },
    };
  }

  Map<String, dynamic> _staffToJson(WorkforceStaffSnapshot staff) {
    return {
      'id': staff.id,
      'display_name': staff.displayName,
      'role_title': staff.roleTitle,
    };
  }

  Map<String, dynamic> _appointmentToJson(AppointmentSnapshot appointment) {
    return {
      'id': appointment.id,
      'status': appointment.status,
      'starts_at': appointment.startsAt,
      'ends_at': appointment.endsAt,
      'staff_profile_id': appointment.staffProfileId,
      'service_item_id': appointment.serviceItemId,
      'customer_id': appointment.customerId,
      'customer_name': appointment.customerName,
    };
  }

  Map<String, dynamic> _deliveryOrderToJson(DeliveryOrderSnapshot order) {
    return {
      'id': order.id,
      'channel_key': order.channelKey,
      'external_order_id': order.externalOrderId,
      'status': order.status,
      'order_id': order.orderId,
    };
  }

  Map<String, dynamic> _businessDaySummaryToJson(
    BusinessDaySummarySnapshot summary,
  ) {
    return {
      'business_date': summary.businessDate,
      'open_orders_count': summary.openOrdersCount,
      'paid_orders_count': summary.paidOrdersCount,
      'gross_sales_minor': summary.grossSalesMinor,
      'cash_sales_minor': summary.cashSalesMinor,
      'open_register_sessions_count': summary.openRegisterSessionsCount,
      'open_exception_cases_count': summary.openExceptionCasesCount,
    };
  }

  Map<String, dynamic> _openExceptionsToJson(OpenExceptionSummary summary) {
    return {
      'count': summary.count,
      'cases': summary.cases
          .map(
            (item) => {
              'id': item.id,
              'type': item.type,
              'severity': item.severity,
              'message': item.message,
              'record_type': item.recordType,
              'record_id': item.recordId,
            },
          )
          .toList(growable: false),
    };
  }

  Map<String, dynamic> _asMap(Object? value) {
    if (value is Map<String, dynamic>) {
      return value;
    }

    if (value is Map) {
      return value.cast<String, dynamic>();
    }

    return const <String, dynamic>{};
  }

  Future<void> _checkoutOrder({
    required Future<List<Map<String, dynamic>>> Function(OrderSummary order)
    buildTenders,
    required String successMessage,
    VoidCallback? onInDoubt,
  }) async {
    final session = _activeRegisterSession;

    if (session == null) {
      _errorMessage = 'Open a register session before checking out.';
      notifyListeners();
      return;
    }

    if (_cart.isEmpty) {
      _errorMessage = 'Add at least one catalog item to the cart first.';
      notifyListeners();
      return;
    }

    await _runBusy(() async {
      final order = await _gateway.createOrder(
        registerSessionId: session.id,
        customerId: _selectedCustomer?.id,
        discountRuleId: _selectedDiscount?.id,
        lines: cartLines
            .map(
              (line) => {
                'catalog_item_id': line.item.id,
                'quantity': line.quantity,
              },
            )
            .toList(growable: false),
      );
      final tenders = await buildTenders(order);

      if (tenders.isEmpty) {
        onInDoubt?.call();
        await _refreshCounts();
        await _refreshCloudStateInternal();
        return;
      }

      final receipt = await _gateway.tenderOrder(
        orderId: order.id,
        tenders: tenders,
      );

      await _syncOutboxRepository.enqueue(
        localEventId: _localEventId(),
        entityType: 'order',
        action: 'tendered',
        payload: {
          'order_id': order.id,
          'customer_id': order.customerId,
          'discount_minor': order.discountMinor,
          'receipt_id': receipt.receiptId,
          'total_minor': order.totalMinor,
          'tip_minor': receipt.payload['tip_minor'] as int? ?? 0,
          'payment_methods': receipt.payments
              .map((payment) => payment.method)
              .toList(),
          'payment_ids': receipt.payments
              .map((payment) => payment.paymentId)
              .toList(),
        },
      );

      await _queueDocumentPrint(
        documentId: receipt.receiptId,
        routeKey: defaultReceiptRoute?.routeKey ?? 'receipt-default',
        payload: {
          'route_key': defaultReceiptRoute?.routeKey ?? 'receipt-default',
          'printer_names':
              defaultReceiptRoute?.printerNames ?? const <String>[],
          'receipt_number': receipt.receiptNumber,
          ...receipt.payload,
        },
      );

      _lastReceipt = receipt;
      _cart.clear();
      await _printPendingReceipts();
      await _refreshCounts();
      await _refreshCloudStateInternal();
      _inDoubtOrderId = null;
      _statusMessage = successMessage;
    });
  }

  Future<void> _queueDocumentPrint({
    required String documentId,
    required String routeKey,
    required Map<String, dynamic> payload,
  }) async {
    await _receiptPrintQueueRepository.enqueue(
      id: documentId,
      receiptId: documentId,
      routeKey: routeKey,
      payload: payload,
    );
  }

  void _patchLastReceiptPaymentStatus(String paymentId, String status) {
    final receipt = _lastReceipt;

    if (receipt == null) {
      return;
    }

    final payload = Map<String, dynamic>.from(receipt.payload);
    final payments =
        (payload['payments'] as List<dynamic>? ?? const <dynamic>[])
            .map(
              (item) => Map<String, dynamic>.from(
                (item as Map).cast<String, dynamic>(),
              ),
            )
            .toList(growable: false);

    final patchedPayments = payments
        .map((payment) {
          if (payment['payment_id'] == paymentId) {
            return {...payment, 'status': status};
          }

          return payment;
        })
        .toList(growable: false);

    _lastReceipt = ReceiptSummary(
      receiptId: receipt.receiptId,
      receiptNumber: receipt.receiptNumber,
      payload: {...payload, 'payments': patchedPayments},
    );
  }

  Future<void> _runBusy(Future<void> Function() operation) async {
    _isBusy = true;
    _errorMessage = null;
    _statusMessage = null;
    notifyListeners();

    try {
      await operation();
    } on PosApiException catch (exception) {
      _errorMessage = exception.message;

      if (exception.requiresReauth) {
        await _enterAuthHoldState();
      }
    } catch (exception) {
      _errorMessage = exception.toString();
    } finally {
      _credentials = await _credentialsStore.load();
      await _refreshCounts();
      _isBusy = false;
      notifyListeners();
    }
  }

  String _localEventId() {
    final random = Random.secure();
    final suffix = random.nextInt(1 << 32).toRadixString(16).padLeft(8, '0');

    return '${DateTime.now().toUtc().microsecondsSinceEpoch}-$suffix';
  }

  Future<void> _enterAuthHoldState() async {
    final shouldRotate = !_requiresReauth;

    _requiresReauth = true;
    await _securityStateStore.setRequiresReauth(true);
    await _syncOutboxRepository.movePendingToAuthHold();

    if (shouldRotate) {
      try {
        await _dataProtectionService.rotateForTrigger('device_auth_revoked');
      } catch (exception) {
        _errorMessage ??=
            'Local encrypted storage rotation failed: ${exception.toString()}';
      }
    }

    await _refreshCounts();
  }
}
