import 'package:flutter/material.dart';

import '../../core/models/pos_models.dart';
import 'pos_home_controller.dart';

class PosHomeScreen extends StatefulWidget {
  const PosHomeScreen({required this.controller, super.key});

  final PosHomeController controller;

  @override
  State<PosHomeScreen> createState() => _PosHomeScreenState();
}

class _PosHomeScreenState extends State<PosHomeScreen> {
  late final TextEditingController _apiBaseUrlController;
  late final TextEditingController _deviceNameController;
  late final TextEditingController _enrollmentCodeController;
  late final TextEditingController _openingFloatController;
  late final TextEditingController _countedCashController;
  late final TextEditingController _tenderedCashController;
  late final TextEditingController _cardTipController;
  late final TextEditingController _splitCashAppliedController;
  late final TextEditingController _splitCashTenderedController;
  late final TextEditingController _splitCardTipController;
  late final TextEditingController _giftCardCodeController;
  late final TextEditingController _giftCardIssueAmountController;
  late final TextEditingController _giftCardRequestedCodeController;
  late final TextEditingController _giftCardTopUpAmountController;
  late final TextEditingController _membershipLookupController;
  late final TextEditingController _membershipNumberController;
  late final TextEditingController _refundAmountController;
  late final TextEditingController _refundReasonController;
  late final TextEditingController _customerSearchController;
  late final TextEditingController _tablePartyNameController;
  late final TextEditingController _tableGuestCountController;
  late final TextEditingController _staffProfileIdController;
  late final TextEditingController _shiftIdController;
  late final TextEditingController _shiftCashController;
  late final TextEditingController _retailSkuController;
  late final TextEditingController _retailQuantityDeltaController;
  late final TextEditingController _retailDocumentNumberController;
  late final TextEditingController _retailDestinationStoreController;
  late final TextEditingController _retailReasonController;

  @override
  void initState() {
    super.initState();
    _apiBaseUrlController = TextEditingController(
      // Prefer the already-enrolled URL; otherwise fall back to the build-time
      // default supplied via `--dart-define=API_BASE_URL=...`. Lets us ship an
      // APK that's pre-pointed at production while still allowing manual edits.
      text: widget.controller.bootstrapSnapshot?.apiBaseUrl
          ?? const String.fromEnvironment('API_BASE_URL', defaultValue: ''),
    );
    _deviceNameController = TextEditingController(text: 'Front Register');
    _enrollmentCodeController = TextEditingController();
    _openingFloatController = TextEditingController();
    _countedCashController = TextEditingController();
    _tenderedCashController = TextEditingController();
    _cardTipController = TextEditingController();
    _splitCashAppliedController = TextEditingController();
    _splitCashTenderedController = TextEditingController();
    _splitCardTipController = TextEditingController();
    _giftCardCodeController = TextEditingController();
    _giftCardIssueAmountController = TextEditingController();
    _giftCardRequestedCodeController = TextEditingController();
    _giftCardTopUpAmountController = TextEditingController();
    _membershipLookupController = TextEditingController();
    _membershipNumberController = TextEditingController();
    _refundAmountController = TextEditingController();
    _refundReasonController = TextEditingController();
    _customerSearchController = TextEditingController();
    _tablePartyNameController = TextEditingController();
    _tableGuestCountController = TextEditingController();
    _staffProfileIdController = TextEditingController();
    _shiftIdController = TextEditingController();
    _shiftCashController = TextEditingController();
    _retailSkuController = TextEditingController();
    _retailQuantityDeltaController = TextEditingController();
    _retailDocumentNumberController = TextEditingController();
    _retailDestinationStoreController = TextEditingController();
    _retailReasonController = TextEditingController();
  }

  @override
  void dispose() {
    _apiBaseUrlController.dispose();
    _deviceNameController.dispose();
    _enrollmentCodeController.dispose();
    _openingFloatController.dispose();
    _countedCashController.dispose();
    _tenderedCashController.dispose();
    _cardTipController.dispose();
    _splitCashAppliedController.dispose();
    _splitCashTenderedController.dispose();
    _splitCardTipController.dispose();
    _giftCardCodeController.dispose();
    _giftCardIssueAmountController.dispose();
    _giftCardRequestedCodeController.dispose();
    _giftCardTopUpAmountController.dispose();
    _membershipLookupController.dispose();
    _membershipNumberController.dispose();
    _refundAmountController.dispose();
    _refundReasonController.dispose();
    _customerSearchController.dispose();
    _tablePartyNameController.dispose();
    _tableGuestCountController.dispose();
    _staffProfileIdController.dispose();
    _shiftIdController.dispose();
    _shiftCashController.dispose();
    _retailSkuController.dispose();
    _retailQuantityDeltaController.dispose();
    _retailDocumentNumberController.dispose();
    _retailDestinationStoreController.dispose();
    _retailReasonController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final controller = widget.controller;
        final snapshot = controller.bootstrapSnapshot;
        final isBusy = controller.isBusy || controller.isLoading;

        return Scaffold(
          backgroundColor: const Color(0xFFF4F0E8),
          appBar: AppBar(
            backgroundColor: const Color(0xFF1F3A2E),
            foregroundColor: Colors.white,
            title: const Text('POS Operations'),
            actions: [
              IconButton(
                tooltip: 'Reload local state',
                onPressed: isBusy ? null : controller.load,
                icon: const Icon(Icons.refresh),
              ),
            ],
          ),
          body: SafeArea(
            child: RefreshIndicator(
              onRefresh: controller.isEnrolled
                  ? controller.refreshCloudState
                  : controller.load,
              child: ListView(
                padding: const EdgeInsets.all(20),
                children: [
                  _HeroCard(
                    title: 'Store Operations Console',
                    body: controller.isEnrolled
                        ? 'Register, table, tendering, workforce, delivery, retail, sync recovery, and print operations stay on one device surface.'
                        : 'Enroll the device with a store-scoped code to unlock the approved operating surface.',
                  ),
                  if (controller.errorMessage != null) ...[
                    const SizedBox(height: 16),
                    _MessageBanner(
                      title: 'Attention',
                      message: controller.errorMessage!,
                      color: const Color(0xFF8A2D1C),
                      background: const Color(0xFFF8DED7),
                    ),
                  ],
                  if (controller.statusMessage != null) ...[
                    const SizedBox(height: 16),
                    _MessageBanner(
                      title: 'Status',
                      message: controller.statusMessage!,
                      color: const Color(0xFF1F5A44),
                      background: const Color(0xFFDBEFE5),
                    ),
                  ],
                  const SizedBox(height: 20),
                  if (!controller.isEnrolled || controller.requiresReauth)
                    _EnrollmentCard(
                      apiBaseUrlController: _apiBaseUrlController,
                      deviceNameController: _deviceNameController,
                      enrollmentCodeController: _enrollmentCodeController,
                      isBusy: isBusy,
                      requiresReauth: controller.requiresReauth,
                      heldEventCount: controller.authHoldSyncEvents,
                      onEnroll: () => controller.enroll(
                        apiBaseUrl: _apiBaseUrlController.text,
                        enrollmentCode: _enrollmentCodeController.text,
                        deviceName: _deviceNameController.text,
                      ),
                    )
                  else ...[
                    _SectionTitle(
                      title: 'Device Status',
                      subtitle:
                          'Cloud state stays authoritative while cached config, the sync queue, and the print spool remain on-device.',
                    ),
                    const SizedBox(height: 12),
                    Wrap(
                      spacing: 12,
                      runSpacing: 12,
                      children: [
                        _StatusCard(
                          title: 'Bootstrap Cache',
                          value: snapshot == null ? 'Empty' : 'Ready',
                          subtitle: snapshot == null
                              ? 'Run cloud refresh to cache the latest store state.'
                              : 'Store ${snapshot.storeId ?? 'unknown'} synced at ${snapshot.syncedAt.toLocal()}.',
                        ),
                        _StatusCard(
                          title: 'Sync Queue',
                          value: '${controller.pendingSyncEvents}',
                          subtitle:
                              'Pending operational events waiting for push.',
                        ),
                        _StatusCard(
                          title: 'Auth Hold',
                          value: '${controller.authHoldSyncEvents}',
                          subtitle:
                              'Held sync events waiting for manager approval.',
                        ),
                        _StatusCard(
                          title: 'Receipt Spool',
                          value: '${controller.pendingPrintJobs}',
                          subtitle:
                              'Local receipt jobs awaiting print execution.',
                        ),
                        _StatusCard(
                          title: 'Open Exceptions',
                          value: '${controller.openExceptions?.count ?? 0}',
                          subtitle:
                              'Manual-review items surfaced from cloud operations.',
                        ),
                        _StatusCard(
                          title: 'Tender Provider',
                          value:
                              controller.paymentCapabilities?.defaultProvider ??
                              'Unavailable',
                          subtitle:
                              controller
                                      .paymentCapabilities
                                      ?.supportedTenders
                                      .isNotEmpty ??
                                  false
                              ? 'Tenders: ${controller.paymentCapabilities!.supportedTenders.join(', ')}'
                              : 'Refresh cloud state to load payment capabilities.',
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    _ActionRow(
                      isBusy: isBusy,
                      onRefreshCloud: controller.refreshCloudState,
                      onSync: controller.syncNow,
                      onPrint: controller.printPendingReceipts,
                      onRecovery: controller.startSyncRecovery,
                    ),
                    if (controller.authHoldSyncEvents > 0) ...[
                      const SizedBox(height: 20),
                      _AuthHoldCard(
                        heldEventCount: controller.authHoldSyncEvents,
                        isBusy: isBusy,
                        onApprove: controller.approveHeldSyncEvents,
                      ),
                    ],
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Business Day',
                      subtitle:
                          'This card mirrors the cloud-authoritative business date summary and the current exception queue.',
                    ),
                    const SizedBox(height: 12),
                    _BusinessDayCard(
                      summary: controller.businessDaySummary,
                      exceptions: controller.openExceptions,
                    ),
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Register Session',
                      subtitle:
                          'Drawer ownership stays on one device, and close uses the current cloud session version.',
                    ),
                    const SizedBox(height: 12),
                    _RegisterCard(
                      session: controller.activeRegisterSession,
                      openingFloatController: _openingFloatController,
                      countedCashController: _countedCashController,
                      isBusy: isBusy,
                      onOpen: () => controller.openRegister(
                        int.tryParse(_openingFloatController.text) ?? 0,
                      ),
                      onClose: () => controller.closeRegister(
                        int.tryParse(_countedCashController.text) ?? 0,
                      ),
                    ),
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Dining Tables',
                      subtitle:
                          'Claimed tables stay cloud-locked per device, with lease heartbeats and explicit release.',
                    ),
                    const SizedBox(height: 12),
                    _DiningTablesCard(
                      tables: controller.diningTables,
                      activeClaimedTable: controller.activeClaimedTable,
                      isBusy: isBusy,
                      partyNameController: _tablePartyNameController,
                      guestCountController: _tableGuestCountController,
                      onClaim: (tableId) => controller.claimDiningTable(
                        diningTableId: tableId,
                        currentPartyName: _tablePartyNameController.text,
                        guestCount: int.tryParse(
                          _tableGuestCountController.text,
                        ),
                      ),
                      onRelease: controller.releaseDiningTable,
                    ),
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Workforce, Delivery & Retail',
                      subtitle:
                          'Phase 3/4 operations use cloud-authoritative endpoints and keep local state refreshed for offline-safe visibility.',
                    ),
                    const SizedBox(height: 12),
                    _AdvancedOperationsCard(
                      staff: controller.workforceStaff,
                      appointments: controller.appointments,
                      laborAnalytics: controller.laborAnalytics,
                      deliveryOrders: controller.deliveryOrders,
                      lastRetailOperation: controller.lastRetailOperation,
                      staffProfileIdController: _staffProfileIdController,
                      shiftIdController: _shiftIdController,
                      shiftCashController: _shiftCashController,
                      skuController: _retailSkuController,
                      quantityDeltaController: _retailQuantityDeltaController,
                      documentNumberController: _retailDocumentNumberController,
                      destinationStoreController:
                          _retailDestinationStoreController,
                      reasonController: _retailReasonController,
                      isBusy: isBusy,
                      onRefresh: controller.refreshPhaseThreeFourOperations,
                      onCheckInAppointment: controller.checkInAppointment,
                      onCompleteAppointment: controller.completeAppointment,
                      onOpenShift: () => controller.openStaffShift(
                        staffProfileId: _staffProfileIdController.text,
                        openingCashMinor: int.tryParse(
                          _shiftCashController.text,
                        ),
                      ),
                      onCloseShift: () => controller.closeStaffShift(
                        shiftId: _shiftIdController.text,
                        closingCashMinor: int.tryParse(
                          _shiftCashController.text,
                        ),
                        notes: _retailReasonController.text,
                      ),
                      onPauseDelivery: () =>
                          controller.setStoreDeliveryAvailability(false),
                      onResumeDelivery: () =>
                          controller.setStoreDeliveryAvailability(true),
                      onConfirmDelivery: controller.confirmDeliveryOrder,
                      onMarkDeliveryReady: (linkId) =>
                          controller.updateDeliveryOrderStatus(
                            linkId: linkId,
                            status: 'ready',
                          ),
                      onLookupRetail: () => controller.lookupRetailInventory(
                        sku: _retailSkuController.text,
                      ),
                      onReceiveRetail: () => controller.receiveRetailStock(
                        documentNumber: _retailDocumentNumberController.text,
                        sku: _retailSkuController.text,
                        quantity:
                            int.tryParse(_retailQuantityDeltaController.text) ??
                            0,
                        reason: _retailReasonController.text,
                      ),
                      onTransferRetail: () => controller.transferRetailStock(
                        destinationStoreId:
                            _retailDestinationStoreController.text,
                        documentNumber: _retailDocumentNumberController.text,
                        sku: _retailSkuController.text,
                        quantity:
                            int.tryParse(_retailQuantityDeltaController.text) ??
                            0,
                        reason: _retailReasonController.text,
                      ),
                      onAdjustRetail: () => controller.adjustRetailStock(
                        sku: _retailSkuController.text,
                        quantityDelta:
                            int.tryParse(_retailQuantityDeltaController.text) ??
                            0,
                        reason: _retailReasonController.text,
                      ),
                      onReturnRetail: () => controller.processRetailReturn(
                        documentNumber: _retailDocumentNumberController.text,
                        sku: _retailSkuController.text,
                        quantity:
                            int.tryParse(_retailQuantityDeltaController.text) ??
                            0,
                        reason: _retailReasonController.text,
                      ),
                    ),
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Customer & Discount',
                      subtitle:
                          'Search results stay cloud-backed while the selected customer and discount live on the device until checkout.',
                    ),
                    const SizedBox(height: 12),
                    _CustomerDiscountCard(
                      customerSearchController: _customerSearchController,
                      results: controller.customerResults,
                      selectedCustomer: controller.selectedCustomer,
                      selectedDiscount: controller.selectedDiscount,
                      discountRules: controller.discountRules,
                      isBusy: isBusy,
                      onSearch: () => controller.searchCustomers(
                        _customerSearchController.text,
                      ),
                      onSelectCustomer: controller.selectCustomer,
                      onSelectDiscount: controller.selectDiscount,
                    ),
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Stored Value & Memberships',
                      subtitle:
                          'Gift cards and memberships stay online-only, with merchant-wide validation and label printing routed through the same local spool.',
                    ),
                    const SizedBox(height: 12),
                    _StoredValueCard(
                      selectedCustomer: controller.selectedCustomer,
                      giftCardSnapshot: controller.giftCardSnapshot,
                      membershipLookup: controller.membershipLookup,
                      membershipPlans: controller.membershipPlans,
                      selectedMembershipPlan: controller.selectedMembershipPlan,
                      giftCardCodeController: _giftCardCodeController,
                      giftCardIssueAmountController:
                          _giftCardIssueAmountController,
                      giftCardRequestedCodeController:
                          _giftCardRequestedCodeController,
                      giftCardTopUpAmountController:
                          _giftCardTopUpAmountController,
                      membershipLookupController: _membershipLookupController,
                      membershipNumberController: _membershipNumberController,
                      isBusy: isBusy,
                      onLookupGiftCard: () => controller.lookupGiftCard(
                        _giftCardCodeController.text,
                      ),
                      onIssueGiftCard: () => controller.issueGiftCard(
                        amountMinor:
                            int.tryParse(_giftCardIssueAmountController.text) ??
                            0,
                        requestedCode: _giftCardRequestedCodeController.text,
                      ),
                      onTopUpGiftCard: () => controller.topUpGiftCard(
                        giftCardCode: _giftCardCodeController.text,
                        amountMinor:
                            int.tryParse(_giftCardTopUpAmountController.text) ??
                            0,
                      ),
                      onLookupMembership: () => controller.lookupMembership(
                        memberNumber: _membershipLookupController.text,
                        customerId: controller.selectedCustomer?.id,
                      ),
                      onSelectMembershipPlan: controller.selectMembershipPlan,
                      onActivateMembership: () => controller.activateMembership(
                        memberNumber: _membershipNumberController.text,
                      ),
                    ),
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Catalog & Tendering',
                      subtitle:
                          'Catalog totals still follow the server contract, and Phase 2 adds card, split, stored-value, refund, and void controls on top of the same cart.',
                    ),
                    const SizedBox(height: 12),
                    _CatalogCard(
                      items: controller.catalogItems,
                      cartLines: controller.cartLines,
                      subtotalMinor: controller.cartEstimatedSubtotalMinor,
                      discountMinor: controller.cartEstimatedDiscountMinor,
                      taxMinor: controller.cartEstimatedTaxMinor,
                      totalMinor: controller.cartEstimatedTotalMinor,
                      paymentCapabilities: controller.paymentCapabilities,
                      tenderedCashController: _tenderedCashController,
                      cardTipController: _cardTipController,
                      splitCashAppliedController: _splitCashAppliedController,
                      splitCashTenderedController: _splitCashTenderedController,
                      splitCardTipController: _splitCardTipController,
                      giftCardCodeController: _giftCardCodeController,
                      refundAmountController: _refundAmountController,
                      refundReasonController: _refundReasonController,
                      lastReceipt: controller.lastReceipt,
                      isBusy: isBusy,
                      onAddItem: controller.addItem,
                      onRemoveItem: controller.removeItem,
                      onCheckout: () => controller.checkoutCash(
                        int.tryParse(_tenderedCashController.text) ?? 0,
                      ),
                      onCheckoutCard: () => controller.checkoutCard(
                        int.tryParse(_cardTipController.text) ?? 0,
                      ),
                      onCheckoutSplit: () => controller.checkoutSplitCashCard(
                        cashAppliedMinor:
                            int.tryParse(_splitCashAppliedController.text) ?? 0,
                        cashTenderedMinor:
                            int.tryParse(_splitCashTenderedController.text) ??
                            0,
                        cardTipMinor:
                            int.tryParse(_splitCardTipController.text) ?? 0,
                      ),
                      onCheckoutGiftCard: () => controller.checkoutGiftCard(
                        _giftCardCodeController.text,
                      ),
                      onRefund: (paymentId) => controller.refundPayment(
                        paymentId: paymentId,
                        amountMinor: int.tryParse(_refundAmountController.text),
                        reason: _refundReasonController.text,
                      ),
                      onVoid: (paymentId) => controller.voidPayment(
                        paymentId: paymentId,
                        reason: _refundReasonController.text,
                      ),
                    ),
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Print Routes',
                      subtitle:
                          'Receipts queue locally against cloud-defined route keys with primary and secondary printer targets.',
                    ),
                    const SizedBox(height: 12),
                    _PrintRoutesCard(routes: controller.printRoutes),
                    const SizedBox(height: 20),
                    _SectionTitle(
                      title: 'Sync Recovery',
                      subtitle:
                          'Recovery runs replay unresolved sync events through the cloud batch runner without blocking the register surface.',
                    ),
                    const SizedBox(height: 12),
                    _RecoveryCard(run: controller.lastSyncRecoveryRun),
                    if (controller.lastReceipt != null) ...[
                      const SizedBox(height: 20),
                      _ReceiptCard(receipt: controller.lastReceipt!),
                    ],
                  ],
                  if (isBusy) ...[
                    const SizedBox(height: 20),
                    Center(
                      child: Column(
                        children: [
                          const CircularProgressIndicator(),
                          const SizedBox(height: 12),
                          Text(
                            'Applying Phase 2 device work...',
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: const Color(0xFF4A564E),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}

class _HeroCard extends StatelessWidget {
  const _HeroCard({required this.title, required this.body});

  final String title;
  final String body;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF1F3A2E), Color(0xFF2D5C47)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(28),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: theme.textTheme.headlineSmall?.copyWith(
              color: Colors.white,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            body,
            style: theme.textTheme.bodyLarge?.copyWith(
              color: const Color(0xFFEAF4EF),
              height: 1.4,
            ),
          ),
        ],
      ),
    );
  }
}

class _MessageBanner extends StatelessWidget {
  const _MessageBanner({
    required this.title,
    required this.message,
    required this.color,
    required this.background,
  });

  final String title;
  final String message;
  final Color color;
  final Color background;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.25)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(
              color: color,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            message,
            style: theme.textTheme.bodyMedium?.copyWith(color: color),
          ),
        ],
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title, required this.subtitle});

  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: theme.textTheme.headlineSmall?.copyWith(
            color: const Color(0xFF1F3A2E),
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: 6),
        Text(
          subtitle,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: const Color(0xFF4A564E),
          ),
        ),
      ],
    );
  }
}

class _StatusCard extends StatelessWidget {
  const _StatusCard({
    required this.title,
    required this.value,
    required this.subtitle,
  });

  final String title;
  final String value;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return SizedBox(
      width: 220,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: const Color(0xFFD7D1C4)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: theme.textTheme.labelLarge?.copyWith(
                color: const Color(0xFF4A564E),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              value,
              style: theme.textTheme.headlineSmall?.copyWith(
                color: const Color(0xFF1F3A2E),
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              subtitle,
              style: theme.textTheme.bodySmall?.copyWith(
                color: const Color(0xFF5F6C64),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ActionRow extends StatelessWidget {
  const _ActionRow({
    required this.isBusy,
    required this.onRefreshCloud,
    required this.onSync,
    required this.onPrint,
    required this.onRecovery,
  });

  final bool isBusy;
  final VoidCallback onRefreshCloud;
  final VoidCallback onSync;
  final VoidCallback onPrint;
  final VoidCallback onRecovery;

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: [
        FilledButton.icon(
          onPressed: isBusy ? null : onRefreshCloud,
          icon: const Icon(Icons.cloud_download_outlined),
          label: const Text('Refresh Cloud'),
        ),
        FilledButton.tonalIcon(
          onPressed: isBusy ? null : onSync,
          icon: const Icon(Icons.sync),
          label: const Text('Sync Now'),
        ),
        FilledButton.tonalIcon(
          onPressed: isBusy ? null : onPrint,
          icon: const Icon(Icons.print_outlined),
          label: const Text('Run Print Spool'),
        ),
        FilledButton.tonalIcon(
          onPressed: isBusy ? null : onRecovery,
          icon: const Icon(Icons.auto_fix_high_outlined),
          label: const Text('Run Sync Recovery'),
        ),
      ],
    );
  }
}

class _EnrollmentCard extends StatelessWidget {
  const _EnrollmentCard({
    required this.apiBaseUrlController,
    required this.deviceNameController,
    required this.enrollmentCodeController,
    required this.isBusy,
    required this.requiresReauth,
    required this.heldEventCount,
    required this.onEnroll,
  });

  final TextEditingController apiBaseUrlController;
  final TextEditingController deviceNameController;
  final TextEditingController enrollmentCodeController;
  final bool isBusy;
  final bool requiresReauth;
  final int heldEventCount;
  final VoidCallback onEnroll;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            requiresReauth
                ? 'Device Re-Enrollment Required'
                : 'Device Enrollment',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              color: const Color(0xFF1F3A2E),
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 16),
          if (requiresReauth || heldEventCount > 0) ...[
            Text(
              requiresReauth
                  ? 'This device is locked. Pending sync events were moved to encrypted auth hold and stay quarantined until re-enrollment completes.'
                  : '$heldEventCount sync events remain quarantined and need manager approval before they can be pushed.',
              style: const TextStyle(color: Color(0xFF5F6C64)),
            ),
            const SizedBox(height: 12),
          ],
          TextField(
            controller: apiBaseUrlController,
            decoration: const InputDecoration(labelText: 'API Base URL'),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: deviceNameController,
            decoration: const InputDecoration(labelText: 'Device Name'),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: enrollmentCodeController,
            decoration: const InputDecoration(labelText: 'Enrollment Code'),
          ),
          const SizedBox(height: 16),
          FilledButton(
            onPressed: isBusy ? null : onEnroll,
            child: Text(requiresReauth ? 'Re-Enroll Device' : 'Enroll Device'),
          ),
        ],
      ),
    );
  }
}

class _AuthHoldCard extends StatelessWidget {
  const _AuthHoldCard({
    required this.heldEventCount,
    required this.isBusy,
    required this.onApprove,
  });

  final int heldEventCount;
  final bool isBusy;
  final VoidCallback onApprove;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Held Sync Events',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              color: const Color(0xFF1F3A2E),
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            '$heldEventCount encrypted sync events remain in quarantine after re-auth. They are preserved on-device and will not upload until a manager approves release.',
            style: const TextStyle(color: Color(0xFF4A564E)),
          ),
          const SizedBox(height: 14),
          FilledButton.tonalIcon(
            onPressed: isBusy ? null : onApprove,
            icon: const Icon(Icons.verified_user_outlined),
            label: const Text('Approve Held Events'),
          ),
        ],
      ),
    );
  }
}

class _RegisterCard extends StatelessWidget {
  const _RegisterCard({
    required this.session,
    required this.openingFloatController,
    required this.countedCashController,
    required this.isBusy,
    required this.onOpen,
    required this.onClose,
  });

  final RegisterSessionSnapshot? session;
  final TextEditingController openingFloatController;
  final TextEditingController countedCashController;
  final bool isBusy;
  final VoidCallback onOpen;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (session != null) ...[
            Text(
              'Active Session ${session!.id}',
              style: theme.textTheme.titleLarge?.copyWith(
                color: const Color(0xFF1F3A2E),
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 12,
              runSpacing: 8,
              children: [
                _MetricPill(
                  label: 'Business Date',
                  value: session!.businessDate ?? 'Unknown',
                ),
                _MetricPill(
                  label: 'Session Version',
                  value: '${session!.sessionVersion}',
                ),
                _MetricPill(
                  label: 'Expected Cash',
                  value: _formatMinor(session!.expectedCashMinor),
                ),
              ],
            ),
          ] else
            TextField(
              controller: openingFloatController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Opening Float Minor Units',
              ),
            ),
          if (session != null) ...[
            const SizedBox(height: 16),
            TextField(
              controller: countedCashController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Counted Cash Minor Units',
              ),
            ),
          ],
          const SizedBox(height: 16),
          FilledButton.icon(
            onPressed: isBusy
                ? null
                : session == null
                ? onOpen
                : onClose,
            icon: Icon(session == null ? Icons.lock_open : Icons.lock_outline),
            label: Text(session == null ? 'Open Register' : 'Close Register'),
          ),
        ],
      ),
    );
  }
}

class _BusinessDayCard extends StatelessWidget {
  const _BusinessDayCard({required this.summary, required this.exceptions});

  final BusinessDaySummarySnapshot? summary;
  final OpenExceptionSummary? exceptions;

  @override
  Widget build(BuildContext context) {
    if (summary == null) {
      return const _SurfaceCard(
        child: Text('No business-day summary is cached yet.'),
      );
    }

    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: [
              _MetricPill(label: 'Business Date', value: summary!.businessDate),
              _MetricPill(
                label: 'Paid Orders',
                value: '${summary!.paidOrdersCount}',
              ),
              _MetricPill(
                label: 'Open Orders',
                value: '${summary!.openOrdersCount}',
              ),
              _MetricPill(
                label: 'Gross Sales',
                value: _formatMinor(summary!.grossSalesMinor),
              ),
              _MetricPill(
                label: 'Cash Sales',
                value: _formatMinor(summary!.cashSalesMinor),
              ),
            ],
          ),
          if ((exceptions?.cases ?? const []).isNotEmpty) ...[
            const SizedBox(height: 16),
            const Text(
              'Open Exceptions',
              style: TextStyle(
                color: Color(0xFF1F3A2E),
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            ...exceptions!.cases
                .take(3)
                .map(
                  (item) => Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Container(
                          width: 10,
                          height: 10,
                          margin: const EdgeInsets.only(top: 6),
                          decoration: BoxDecoration(
                            color: item.severity == 'high'
                                ? const Color(0xFF8A2D1C)
                                : const Color(0xFFB87B21),
                            shape: BoxShape.circle,
                          ),
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            '${item.type}: ${item.message}',
                            style: const TextStyle(color: Color(0xFF4A564E)),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
          ],
        ],
      ),
    );
  }
}

class _DiningTablesCard extends StatelessWidget {
  const _DiningTablesCard({
    required this.tables,
    required this.activeClaimedTable,
    required this.isBusy,
    required this.partyNameController,
    required this.guestCountController,
    required this.onClaim,
    required this.onRelease,
  });

  final List<DiningTableSnapshot> tables;
  final DiningTableSnapshot? activeClaimedTable;
  final bool isBusy;
  final TextEditingController partyNameController;
  final TextEditingController guestCountController;
  final ValueChanged<String> onClaim;
  final ValueChanged<String> onRelease;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextField(
            controller: partyNameController,
            decoration: const InputDecoration(labelText: 'Party Name'),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: guestCountController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Guest Count'),
          ),
          const SizedBox(height: 16),
          if (tables.isEmpty)
            const Text('No dining tables are cached for this store.')
          else
            ...tables.map(
              (table) => Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: table.lease.isClaimedByCurrentDevice
                        ? const Color(0xFFE3F0E8)
                        : Colors.white,
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: const Color(0xFFD7D1C4)),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        table.name,
                        style: const TextStyle(
                          color: Color(0xFF1F3A2E),
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        '${table.zoneName ?? 'Floor'} · ${table.status} · capacity ${table.capacity}',
                        style: const TextStyle(color: Color(0xFF5F6C64)),
                      ),
                      if (table.currentPartyName != null) ...[
                        const SizedBox(height: 4),
                        Text(
                          'Party: ${table.currentPartyName} (${table.guestCount ?? 0})',
                          style: const TextStyle(color: Color(0xFF5F6C64)),
                        ),
                      ],
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          if (table.lease.isClaimedByCurrentDevice)
                            FilledButton.tonal(
                              onPressed: isBusy
                                  ? null
                                  : () => onRelease(table.id),
                              child: const Text('Release'),
                            )
                          else
                            FilledButton(
                              onPressed: isBusy
                                  ? null
                                  : () => onClaim(table.id),
                              child: const Text('Claim'),
                            ),
                          const SizedBox(width: 12),
                          Text(
                            activeClaimedTable?.id == table.id
                                ? 'Lease heartbeat active'
                                : table.lease.currentHolderDeviceId == null
                                ? 'Unclaimed'
                                : 'Held by another device',
                            style: const TextStyle(color: Color(0xFF4A564E)),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _CustomerDiscountCard extends StatelessWidget {
  const _CustomerDiscountCard({
    required this.customerSearchController,
    required this.results,
    required this.selectedCustomer,
    required this.selectedDiscount,
    required this.discountRules,
    required this.isBusy,
    required this.onSearch,
    required this.onSelectCustomer,
    required this.onSelectDiscount,
  });

  final TextEditingController customerSearchController;
  final List<CustomerSummary> results;
  final CustomerSummary? selectedCustomer;
  final DiscountRuleSnapshot? selectedDiscount;
  final List<DiscountRuleSnapshot> discountRules;
  final bool isBusy;
  final VoidCallback onSearch;
  final ValueChanged<String?> onSelectCustomer;
  final ValueChanged<String?> onSelectDiscount;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: customerSearchController,
                  decoration: const InputDecoration(
                    labelText: 'Search Customer or Member Number',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              FilledButton.tonalIcon(
                onPressed: isBusy ? null : onSearch,
                icon: const Icon(Icons.search),
                label: const Text('Search'),
              ),
            ],
          ),
          const SizedBox(height: 16),
          DropdownButtonFormField<String>(
            initialValue: selectedDiscount?.id,
            decoration: const InputDecoration(labelText: 'Discount Rule'),
            items: [
              const DropdownMenuItem<String>(
                value: '',
                child: Text('No discount'),
              ),
              ...discountRules.map(
                (rule) => DropdownMenuItem<String>(
                  value: rule.id,
                  child: Text(
                    rule.code == null
                        ? rule.name
                        : '${rule.name} (${rule.code})',
                  ),
                ),
              ),
            ],
            onChanged: isBusy
                ? null
                : (value) => onSelectDiscount(
                    value == null || value.isEmpty ? null : value,
                  ),
          ),
          if (selectedCustomer != null) ...[
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: const Color(0xFFE3F0E8),
                borderRadius: BorderRadius.circular(18),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    selectedCustomer!.name,
                    style: const TextStyle(
                      color: Color(0xFF1F3A2E),
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    selectedCustomer!.memberAccount == null
                        ? 'Customer selected'
                        : 'Member ${selectedCustomer!.memberAccount!.memberNumber}',
                    style: const TextStyle(color: Color(0xFF4A564E)),
                  ),
                  const SizedBox(height: 10),
                  FilledButton.tonal(
                    onPressed: isBusy ? null : () => onSelectCustomer(null),
                    child: const Text('Clear Customer'),
                  ),
                ],
              ),
            ),
          ],
          if (results.isNotEmpty) ...[
            const SizedBox(height: 16),
            ...results.map(
              (customer) => ListTile(
                contentPadding: EdgeInsets.zero,
                title: Text(customer.name),
                subtitle: Text(
                  customer.memberAccount == null
                      ? (customer.phone ?? customer.email ?? 'Customer')
                      : 'Member ${customer.memberAccount!.memberNumber}',
                ),
                trailing: FilledButton.tonal(
                  onPressed: isBusy
                      ? null
                      : () => onSelectCustomer(customer.id),
                  child: const Text('Select'),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _StoredValueCard extends StatelessWidget {
  const _StoredValueCard({
    required this.selectedCustomer,
    required this.giftCardSnapshot,
    required this.membershipLookup,
    required this.membershipPlans,
    required this.selectedMembershipPlan,
    required this.giftCardCodeController,
    required this.giftCardIssueAmountController,
    required this.giftCardRequestedCodeController,
    required this.giftCardTopUpAmountController,
    required this.membershipLookupController,
    required this.membershipNumberController,
    required this.isBusy,
    required this.onLookupGiftCard,
    required this.onIssueGiftCard,
    required this.onTopUpGiftCard,
    required this.onLookupMembership,
    required this.onSelectMembershipPlan,
    required this.onActivateMembership,
  });

  final CustomerSummary? selectedCustomer;
  final GiftCardSnapshot? giftCardSnapshot;
  final MembershipLookupSnapshot? membershipLookup;
  final List<MembershipPlanSnapshot> membershipPlans;
  final MembershipPlanSnapshot? selectedMembershipPlan;
  final TextEditingController giftCardCodeController;
  final TextEditingController giftCardIssueAmountController;
  final TextEditingController giftCardRequestedCodeController;
  final TextEditingController giftCardTopUpAmountController;
  final TextEditingController membershipLookupController;
  final TextEditingController membershipNumberController;
  final bool isBusy;
  final VoidCallback onLookupGiftCard;
  final VoidCallback onIssueGiftCard;
  final VoidCallback onTopUpGiftCard;
  final VoidCallback onLookupMembership;
  final ValueChanged<String?> onSelectMembershipPlan;
  final VoidCallback onActivateMembership;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            selectedCustomer == null
                ? 'Select a customer above to bind gift cards or activate memberships.'
                : 'Selected customer: ${selectedCustomer!.name}',
            style: const TextStyle(color: Color(0xFF4A564E)),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: giftCardCodeController,
                  decoration: const InputDecoration(
                    labelText: 'Gift Card Code',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              FilledButton.tonalIcon(
                onPressed: isBusy ? null : onLookupGiftCard,
                icon: const Icon(Icons.search),
                label: const Text('Lookup'),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: giftCardIssueAmountController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Issue Amount Minor Units',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: TextField(
                  controller: giftCardRequestedCodeController,
                  decoration: const InputDecoration(
                    labelText: 'Requested Code (Optional)',
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: giftCardTopUpAmountController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Top-Up Amount Minor Units',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              FilledButton(
                onPressed: isBusy ? null : onIssueGiftCard,
                child: const Text('Issue + Label'),
              ),
              const SizedBox(width: 12),
              FilledButton.tonal(
                onPressed: isBusy ? null : onTopUpGiftCard,
                child: const Text('Top Up'),
              ),
            ],
          ),
          if (giftCardSnapshot != null) ...[
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: const Color(0xFFE3F0E8),
                borderRadius: BorderRadius.circular(18),
              ),
              child: Text(
                'Gift card ${giftCardSnapshot!.code} - balance ${_formatMinor(giftCardSnapshot!.currentBalanceMinor)}',
                style: const TextStyle(color: Color(0xFF1F3A2E)),
              ),
            ),
          ],
          const SizedBox(height: 20),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: membershipLookupController,
                  decoration: const InputDecoration(
                    labelText: 'Member Number Lookup',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              FilledButton.tonalIcon(
                onPressed: isBusy ? null : onLookupMembership,
                icon: const Icon(Icons.badge_outlined),
                label: const Text('Lookup Membership'),
              ),
            ],
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: selectedMembershipPlan?.id,
            decoration: const InputDecoration(labelText: 'Membership Plan'),
            items: [
              const DropdownMenuItem<String>(
                value: '',
                child: Text('No membership plan selected'),
              ),
              ...membershipPlans.map(
                (plan) => DropdownMenuItem<String>(
                  value: plan.id,
                  child: Text(
                    plan.code == null
                        ? '${plan.name} - ${_formatMinor(plan.priceMinor)}'
                        : '${plan.name} (${plan.code}) - ${_formatMinor(plan.priceMinor)}',
                  ),
                ),
              ),
            ],
            onChanged: isBusy
                ? null
                : (value) => onSelectMembershipPlan(
                    value == null || value.isEmpty ? null : value,
                  ),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: membershipNumberController,
                  decoration: const InputDecoration(
                    labelText: 'Member Number (Optional)',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              FilledButton(
                onPressed: isBusy ? null : onActivateMembership,
                child: const Text('Activate Membership'),
              ),
            ],
          ),
          if (membershipLookup != null) ...[
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: const Color(0xFFE3F0E8),
                borderRadius: BorderRadius.circular(18),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Member ${membershipLookup!.memberNumber}',
                    style: const TextStyle(
                      color: Color(0xFF1F3A2E),
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '${membershipLookup!.customerName ?? 'Customer'} - ${membershipLookup!.membershipPlan?.name ?? 'No plan'}',
                    style: const TextStyle(color: Color(0xFF4A564E)),
                  ),
                  if (membershipLookup!.validUntil != null)
                    Text(
                      'Valid until ${membershipLookup!.validUntil}',
                      style: const TextStyle(color: Color(0xFF4A564E)),
                    ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _CatalogCard extends StatelessWidget {
  const _CatalogCard({
    required this.items,
    required this.cartLines,
    required this.subtotalMinor,
    required this.discountMinor,
    required this.taxMinor,
    required this.totalMinor,
    required this.paymentCapabilities,
    required this.tenderedCashController,
    required this.cardTipController,
    required this.splitCashAppliedController,
    required this.splitCashTenderedController,
    required this.splitCardTipController,
    required this.giftCardCodeController,
    required this.refundAmountController,
    required this.refundReasonController,
    required this.lastReceipt,
    required this.isBusy,
    required this.onAddItem,
    required this.onRemoveItem,
    required this.onCheckout,
    required this.onCheckoutCard,
    required this.onCheckoutSplit,
    required this.onCheckoutGiftCard,
    required this.onRefund,
    required this.onVoid,
  });

  final List<CatalogItemSnapshot> items;
  final List<CartLineSnapshot> cartLines;
  final int subtotalMinor;
  final int discountMinor;
  final int taxMinor;
  final int totalMinor;
  final PaymentCapabilitiesSnapshot? paymentCapabilities;
  final TextEditingController tenderedCashController;
  final TextEditingController cardTipController;
  final TextEditingController splitCashAppliedController;
  final TextEditingController splitCashTenderedController;
  final TextEditingController splitCardTipController;
  final TextEditingController giftCardCodeController;
  final TextEditingController refundAmountController;
  final TextEditingController refundReasonController;
  final ReceiptSummary? lastReceipt;
  final bool isBusy;
  final ValueChanged<CatalogItemSnapshot> onAddItem;
  final ValueChanged<CatalogItemSnapshot> onRemoveItem;
  final VoidCallback onCheckout;
  final VoidCallback onCheckoutCard;
  final VoidCallback onCheckoutSplit;
  final VoidCallback onCheckoutGiftCard;
  final ValueChanged<String> onRefund;
  final ValueChanged<String> onVoid;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (items.isEmpty)
            const Text('No catalog items are cached yet.')
          else
            ...items.map((item) {
              CartLineSnapshot? cartLine;

              for (final line in cartLines) {
                if (line.item.id == item.id) {
                  cartLine = line;
                  break;
                }
              }

              return Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: const Color(0xFFD7D1C4)),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              item.name,
                              style: const TextStyle(
                                color: Color(0xFF1F3A2E),
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              '${_formatMinor(item.effectivePriceMinor)}${item.soldOut ? ' · sold out' : ''}',
                              style: const TextStyle(color: Color(0xFF5F6C64)),
                            ),
                          ],
                        ),
                      ),
                      IconButton(
                        onPressed: isBusy ? null : () => onRemoveItem(item),
                        icon: const Icon(Icons.remove_circle_outline),
                      ),
                      Text('${cartLine?.quantity ?? 0}'),
                      IconButton(
                        onPressed: isBusy ? null : () => onAddItem(item),
                        icon: const Icon(Icons.add_circle_outline),
                      ),
                    ],
                  ),
                ),
              );
            }),
          const SizedBox(height: 16),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: [
              _MetricPill(
                label: 'Subtotal',
                value: _formatMinor(subtotalMinor),
              ),
              _MetricPill(
                label: 'Discount',
                value: _formatMinor(discountMinor),
              ),
              _MetricPill(label: 'Tax', value: _formatMinor(taxMinor)),
              _MetricPill(label: 'Total', value: _formatMinor(totalMinor)),
            ],
          ),
          const SizedBox(height: 16),
          Text(
            paymentCapabilities == null
                ? 'Refresh cloud state to load payment capabilities.'
                : 'Provider: ${paymentCapabilities!.defaultProvider} - Tenders: ${paymentCapabilities!.supportedTenders.join(', ')}',
            style: const TextStyle(color: Color(0xFF4A564E)),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: tenderedCashController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              labelText: 'Tendered Cash Minor Units',
            ),
          ),
          const SizedBox(height: 16),
          FilledButton.icon(
            onPressed: isBusy ? null : onCheckout,
            icon: const Icon(Icons.point_of_sale_outlined),
            label: const Text('Checkout Cash Sale'),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: cardTipController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Card Tip Minor Units',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              FilledButton.tonal(
                onPressed: isBusy ? null : onCheckoutCard,
                child: const Text('Checkout Card'),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: splitCashAppliedController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Split Cash Applied',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: TextField(
                  controller: splitCashTenderedController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Split Cash Tendered',
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: splitCardTipController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Split Card Tip',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              FilledButton.tonal(
                onPressed: isBusy ? null : onCheckoutSplit,
                child: const Text('Checkout Split'),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: giftCardCodeController,
                  decoration: const InputDecoration(
                    labelText: 'Gift Card Code for Tender',
                  ),
                ),
              ),
              const SizedBox(width: 12),
              FilledButton.tonal(
                onPressed: isBusy ? null : onCheckoutGiftCard,
                child: const Text('Checkout Gift Card'),
              ),
            ],
          ),
          if (lastReceipt != null && lastReceipt!.payments.isNotEmpty) ...[
            const SizedBox(height: 20),
            const Text(
              'Refund & Void Controls',
              style: TextStyle(
                color: Color(0xFF1F3A2E),
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: refundAmountController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Refund Amount Minor Units',
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: TextField(
                    controller: refundReasonController,
                    decoration: const InputDecoration(
                      labelText: 'Refund or Void Reason',
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ...lastReceipt!.payments.map(
              (payment) => Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: const Color(0xFFD7D1C4)),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '${payment.method} - ${payment.status}',
                        style: const TextStyle(
                          color: Color(0xFF1F3A2E),
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        'Amount ${_formatMinor(payment.amountMinor)}${payment.tipMinor > 0 ? ' - tip ${_formatMinor(payment.tipMinor)}' : ''}',
                        style: const TextStyle(color: Color(0xFF4A564E)),
                      ),
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          FilledButton.tonal(
                            onPressed:
                                isBusy ||
                                    !(payment.status == 'captured' ||
                                        payment.status == 'partially_refunded')
                                ? null
                                : () => onRefund(payment.paymentId),
                            child: const Text('Refund'),
                          ),
                          const SizedBox(width: 12),
                          FilledButton.tonal(
                            onPressed: isBusy || payment.status != 'captured'
                                ? null
                                : () => onVoid(payment.paymentId),
                            child: const Text('Void'),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _AdvancedOperationsCard extends StatelessWidget {
  const _AdvancedOperationsCard({
    required this.staff,
    required this.appointments,
    required this.laborAnalytics,
    required this.deliveryOrders,
    required this.lastRetailOperation,
    required this.staffProfileIdController,
    required this.shiftIdController,
    required this.shiftCashController,
    required this.skuController,
    required this.quantityDeltaController,
    required this.documentNumberController,
    required this.destinationStoreController,
    required this.reasonController,
    required this.isBusy,
    required this.onRefresh,
    required this.onCheckInAppointment,
    required this.onCompleteAppointment,
    required this.onOpenShift,
    required this.onCloseShift,
    required this.onPauseDelivery,
    required this.onResumeDelivery,
    required this.onConfirmDelivery,
    required this.onMarkDeliveryReady,
    required this.onLookupRetail,
    required this.onReceiveRetail,
    required this.onTransferRetail,
    required this.onAdjustRetail,
    required this.onReturnRetail,
  });

  final List<WorkforceStaffSnapshot> staff;
  final List<AppointmentSnapshot> appointments;
  final Map<String, dynamic>? laborAnalytics;
  final List<DeliveryOrderSnapshot> deliveryOrders;
  final RetailOperationSnapshot? lastRetailOperation;
  final TextEditingController staffProfileIdController;
  final TextEditingController shiftIdController;
  final TextEditingController shiftCashController;
  final TextEditingController skuController;
  final TextEditingController quantityDeltaController;
  final TextEditingController documentNumberController;
  final TextEditingController destinationStoreController;
  final TextEditingController reasonController;
  final bool isBusy;
  final VoidCallback onRefresh;
  final ValueChanged<String> onCheckInAppointment;
  final ValueChanged<String> onCompleteAppointment;
  final VoidCallback onOpenShift;
  final VoidCallback onCloseShift;
  final VoidCallback onPauseDelivery;
  final VoidCallback onResumeDelivery;
  final ValueChanged<String> onConfirmDelivery;
  final ValueChanged<String> onMarkDeliveryReady;
  final VoidCallback onLookupRetail;
  final VoidCallback onReceiveRetail;
  final VoidCallback onTransferRetail;
  final VoidCallback onAdjustRetail;
  final VoidCallback onReturnRetail;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: [
              FilledButton.icon(
                onPressed: isBusy ? null : onRefresh,
                icon: const Icon(Icons.sync),
                label: const Text('Refresh Ops'),
              ),
              FilledButton.tonal(
                onPressed: isBusy ? null : onPauseDelivery,
                child: const Text('Pause Delivery'),
              ),
              FilledButton.tonal(
                onPressed: isBusy ? null : onResumeDelivery,
                child: const Text('Resume Delivery'),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: [
              _MetricPill(label: 'Staff', value: '${staff.length}'),
              _MetricPill(
                label: 'Appointments',
                value: '${appointments.length}',
              ),
              _MetricPill(label: 'Delivery', value: '${deliveryOrders.length}'),
              _MetricPill(
                label: 'Labor Sales',
                value: _formatMinor(
                  laborAnalytics?['gross_sales_minor'] as int? ?? 0,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          _OperationsPanel(
            title: 'Appointments',
            child: appointments.isEmpty
                ? const Text('No appointment work is cached for this store.')
                : Column(
                    children: appointments
                        .take(6)
                        .map((appointment) {
                          final canCheckIn =
                              appointment.status == 'booked' ||
                              appointment.status == 'confirmed';
                          final canComplete =
                              appointment.status == 'checked_in' ||
                              appointment.status == 'in_progress';

                          return _OperationListTile(
                            title:
                                appointment.customerName ??
                                appointment.customerId ??
                                'Walk-in',
                            subtitle:
                                '${appointment.status} - ${appointment.startsAt} to ${appointment.endsAt}',
                            trailing: Wrap(
                              spacing: 8,
                              children: [
                                TextButton(
                                  onPressed: isBusy || !canCheckIn
                                      ? null
                                      : () => onCheckInAppointment(
                                          appointment.id,
                                        ),
                                  child: const Text('Check in'),
                                ),
                                TextButton(
                                  onPressed: isBusy || !canComplete
                                      ? null
                                      : () => onCompleteAppointment(
                                          appointment.id,
                                        ),
                                  child: const Text('Complete'),
                                ),
                              ],
                            ),
                          );
                        })
                        .toList(growable: false),
                  ),
          ),
          const SizedBox(height: 14),
          _OperationsPanel(
            title: 'Staff Shifts & Labor',
            child: Column(
              children: [
                if (staff.isNotEmpty)
                  ...staff
                      .take(5)
                      .map(
                        (member) => _OperationListTile(
                          title: member.displayName,
                          subtitle:
                              '${member.roleTitle ?? 'Staff'} - ${member.id}',
                          trailing: TextButton(
                            onPressed: isBusy
                                ? null
                                : () {
                                    staffProfileIdController.text = member.id;
                                    onOpenShift();
                                  },
                            child: const Text('Open shift'),
                          ),
                        ),
                      )
                else
                  const Align(
                    alignment: Alignment.centerLeft,
                    child: Text('No staff profiles are cached for this store.'),
                  ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: staffProfileIdController,
                        decoration: const InputDecoration(
                          labelText: 'Staff Profile ID',
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: TextField(
                        controller: shiftIdController,
                        decoration: const InputDecoration(
                          labelText: 'Shift ID',
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: shiftCashController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Cash Minor Units',
                  ),
                ),
                const SizedBox(height: 10),
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: [
                    FilledButton.tonal(
                      onPressed: isBusy ? null : onOpenShift,
                      child: const Text('Open Shift'),
                    ),
                    FilledButton.tonal(
                      onPressed: isBusy ? null : onCloseShift,
                      child: const Text('Close Shift'),
                    ),
                  ],
                ),
                if (laborAnalytics != null) ...[
                  const SizedBox(height: 12),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: Text(
                      'Labor snapshot: ${laborAnalytics.toString()}',
                      style: const TextStyle(color: Color(0xFF4A564E)),
                    ),
                  ),
                ],
              ],
            ),
          ),
          if (deliveryOrders.isNotEmpty) ...[
            const SizedBox(height: 16),
            _OperationsPanel(
              title: 'Delivery Orders',
              child: Column(
                children: deliveryOrders
                    .take(6)
                    .map((order) {
                      return _OperationListTile(
                        title: '${order.channelKey} ${order.externalOrderId}',
                        subtitle: '${order.status} - link ${order.id}',
                        trailing: Wrap(
                          spacing: 8,
                          children: [
                            TextButton(
                              onPressed: isBusy
                                  ? null
                                  : () => onConfirmDelivery(order.id),
                              child: const Text('Confirm'),
                            ),
                            TextButton(
                              onPressed: isBusy
                                  ? null
                                  : () => onMarkDeliveryReady(order.id),
                              child: const Text('Ready'),
                            ),
                          ],
                        ),
                      );
                    })
                    .toList(growable: false),
              ),
            ),
          ],
          const SizedBox(height: 16),
          _OperationsPanel(
            title: 'Retail Inventory',
            child: Column(
              children: [
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: skuController,
                        decoration: const InputDecoration(labelText: 'SKU'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: TextField(
                        controller: quantityDeltaController,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(labelText: 'Qty'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: documentNumberController,
                        decoration: const InputDecoration(
                          labelText: 'Document Number',
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: TextField(
                        controller: destinationStoreController,
                        decoration: const InputDecoration(
                          labelText: 'Destination Store ID',
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: reasonController,
                  decoration: const InputDecoration(labelText: 'Reason'),
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: [
                    FilledButton.tonal(
                      onPressed: isBusy ? null : onLookupRetail,
                      child: const Text('Lookup'),
                    ),
                    FilledButton.tonal(
                      onPressed: isBusy ? null : onReceiveRetail,
                      child: const Text('Receive'),
                    ),
                    FilledButton.tonal(
                      onPressed: isBusy ? null : onTransferRetail,
                      child: const Text('Transfer'),
                    ),
                    FilledButton.tonal(
                      onPressed: isBusy ? null : onAdjustRetail,
                      child: const Text('Adjust'),
                    ),
                    FilledButton.tonal(
                      onPressed: isBusy ? null : onReturnRetail,
                      child: const Text('Return'),
                    ),
                  ],
                ),
                if (lastRetailOperation != null) ...[
                  const SizedBox(height: 12),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: Text(
                      lastRetailOperation!.payload.toString(),
                      style: const TextStyle(color: Color(0xFF4A564E)),
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _OperationsPanel extends StatelessWidget {
  const _OperationsPanel({required this.title, required this.child});

  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF9F6EF),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0xFFE0D6C6)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(
                color: Color(0xFF1F3A2E),
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 10),
            child,
          ],
        ),
      ),
    );
  }
}

class _OperationListTile extends StatelessWidget {
  const _OperationListTile({
    required this.title,
    required this.subtitle,
    required this.trailing,
  });

  final String title;
  final String subtitle;
  final Widget trailing;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    color: Color(0xFF1F3A2E),
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  subtitle,
                  style: const TextStyle(color: Color(0xFF4A564E)),
                ),
              ],
            ),
          ),
          trailing,
        ],
      ),
    );
  }
}

class _PrintRoutesCard extends StatelessWidget {
  const _PrintRoutesCard({required this.routes});

  final List<PrintRouteSnapshot> routes;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: routes.isEmpty
          ? const Text('No cloud print routes are cached yet.')
          : Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: routes
                  .map(
                    (route) => Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: Text(
                        '${route.routeKey}: ${route.printerNames.join(' -> ')}',
                        style: const TextStyle(color: Color(0xFF4A564E)),
                      ),
                    ),
                  )
                  .toList(growable: false),
            ),
    );
  }
}

class _RecoveryCard extends StatelessWidget {
  const _RecoveryCard({required this.run});

  final SyncRecoveryRunSnapshot? run;

  @override
  Widget build(BuildContext context) {
    return _SurfaceCard(
      child: run == null
          ? const Text('No sync recovery run has been started on this device.')
          : Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Run ${run!.id}',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: const Color(0xFF1F3A2E),
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 8),
                Text('Status: ${run!.status}'),
                Text('Events: ${run!.eventCount}'),
                if (run!.batch != null)
                  Text(
                    'Batch progress: ${run!.batch!.processedJobs}/${run!.batch!.totalJobs} (${run!.batch!.progress}%)',
                  ),
              ],
            ),
    );
  }
}

class _ReceiptCard extends StatelessWidget {
  const _ReceiptCard({required this.receipt});

  final ReceiptSummary receipt;

  @override
  Widget build(BuildContext context) {
    final payload = receipt.payload;

    return _SurfaceCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Last Receipt #${receipt.receiptNumber}',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              color: const Color(0xFF1F3A2E),
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            'Order total: ${_formatMinor(payload['total_minor'] as int? ?? 0)}',
          ),
          Text('Tip: ${_formatMinor(payload['tip_minor'] as int? ?? 0)}'),
          Text('Paid: ${_formatMinor(payload['paid_minor'] as int? ?? 0)}'),
          if (payload['discount_minor'] is int)
            Text('Discount: ${_formatMinor(payload['discount_minor'] as int)}'),
          const SizedBox(height: 10),
          ...receipt.payments.map(
            (payment) => Text(
              '${payment.method}: ${_formatMinor(payment.amountMinor)} (${payment.status})',
            ),
          ),
        ],
      ),
    );
  }
}

class _SurfaceCard extends StatelessWidget {
  const _SurfaceCard({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFFFFFCF8),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFD7D1C4)),
        boxShadow: const [
          BoxShadow(
            color: Color(0x12000000),
            blurRadius: 24,
            offset: Offset(0, 12),
          ),
        ],
      ),
      child: child,
    );
  }
}

class _MetricPill extends StatelessWidget {
  const _MetricPill({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: const Color(0xFFE8E1D4),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(color: Color(0xFF5F6C64), fontSize: 12),
          ),
          const SizedBox(height: 2),
          Text(
            value,
            style: const TextStyle(
              color: Color(0xFF1F3A2E),
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}

String _formatMinor(int amountMinor) {
  final isNegative = amountMinor < 0;
  final absolute = amountMinor.abs();
  final dollars = absolute ~/ 100;
  final cents = (absolute % 100).toString().padLeft(2, '0');

  return '${isNegative ? '-' : ''}\$$dollars.$cents';
}
