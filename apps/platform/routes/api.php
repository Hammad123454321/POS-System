<?php

use App\Modules\Catalog\Interfaces\Http\Controllers\AdminCatalogItemAddOnController;
use App\Modules\Catalog\Interfaces\Http\Controllers\AdminCatalogItemController;
use App\Modules\Catalog\Interfaces\Http\Controllers\AdminCategoryController;
use App\Modules\Catalog\Interfaces\Http\Controllers\AdminComboPackageController;
use App\Modules\Catalog\Interfaces\Http\Controllers\AdminDiscountRuleController;
use App\Modules\Catalog\Interfaces\Http\Controllers\AdminModifierGroupController;
use App\Modules\Catalog\Interfaces\Http\Controllers\AdminModifierOptionController;
use App\Modules\Catalog\Interfaces\Http\Controllers\AdminVariantController;
use App\Modules\Catalog\Interfaces\Http\Controllers\PosConfigController;
use App\Modules\CustomerValue\Interfaces\Http\Controllers\AdminCustomerController;
use App\Modules\CustomerValue\Interfaces\Http\Controllers\AdminCustomerPrivacyController;
use App\Modules\CustomerValue\Interfaces\Http\Controllers\CustomerSearchController;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Controllers\AdminDeliveryChannelConfigController;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Controllers\DeliveryAvailabilityController;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Controllers\DeliveryOrderController;
use App\Modules\ExceptionQueue\Interfaces\Http\Controllers\AdminResolveExceptionCaseController;
use App\Modules\ExceptionQueue\Interfaces\Http\Controllers\OpenExceptionCaseController;
use App\Modules\Identity\Interfaces\Http\Controllers\AdminStoreUserController;
use App\Modules\OfflineSync\Interfaces\Http\Controllers\SyncController;
use App\Modules\OfflineSync\Interfaces\Http\Controllers\SyncRecoveryRunController;
use App\Modules\OrderRegister\Interfaces\Http\Controllers\AdminOrderController;
use App\Modules\OrderRegister\Interfaces\Http\Controllers\OrderCashCheckoutController;
use App\Modules\OrderRegister\Interfaces\Http\Controllers\OrderController;
use App\Modules\OrderRegister\Interfaces\Http\Controllers\RegisterSessionController;
use App\Modules\Payments\Interfaces\Http\Controllers\FiservTransNotifyWebhookController;
use App\Modules\Payments\Interfaces\Http\Controllers\OrderTenderController;
use App\Modules\Payments\Interfaces\Http\Controllers\PaymentInquiryController;
use App\Modules\Payments\Interfaces\Http\Controllers\PaymentRefundController;
use App\Modules\Payments\Interfaces\Http\Controllers\PaymentVoidController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\AdminDeviceController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\AdminDeviceProfileController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\AdminMerchantPrivacyController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\AdminStoreController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\DeviceAuthController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\DeviceEnrollmentCodeController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\DeviceStatusEventController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\PosBootstrapController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\StoreFeatureFlagController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\SuperAdmin\DeviceProfileController as SuperAdminDeviceProfileController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\SuperAdmin\FeatureFlagController as SuperAdminFeatureFlagController;
use App\Modules\PlatformCore\Interfaces\Http\Controllers\SuperAdmin\MerchantController as SuperAdminMerchantController;
use App\Modules\Reporting\Interfaces\Http\Controllers\AdminArchivedAuditLogController;
use App\Modules\Reporting\Interfaces\Http\Controllers\AdminArchivedPayrollSnapshotController;
use App\Modules\Reporting\Interfaces\Http\Controllers\AdminArchivedReceiptController;
use App\Modules\Reporting\Interfaces\Http\Controllers\AdminDeliveryOperationalHealthController;
use App\Modules\Reporting\Interfaces\Http\Controllers\AdminRetailStockMovementSummaryController;
use App\Modules\Reporting\Interfaces\Http\Controllers\BusinessDaySummaryController;
use App\Modules\Restaurant\Interfaces\Http\Controllers\AdminDiningTableController;
use App\Modules\Restaurant\Interfaces\Http\Controllers\AdminPrinterConfigController;
use App\Modules\Restaurant\Interfaces\Http\Controllers\AdminPrintRouteController;
use App\Modules\Restaurant\Interfaces\Http\Controllers\DiningTableController;
use App\Modules\Retail\Interfaces\Http\Controllers\AdminBarcodeRecordController;
use App\Modules\Retail\Interfaces\Http\Controllers\AdminRetailPromotionController;
use App\Modules\Retail\Interfaces\Http\Controllers\RetailInventoryController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminApproveAttendanceRecordController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminCommissionRuleController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminGeneratePayrollSnapshotController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminLaborAnalyticsController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminPayrollSnapshotController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminServiceItemController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminStaffProfileController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminStaffServiceRuleController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AdminWageRuleController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\AppointmentController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\ShiftController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\WorkforceLaborAnalyticsController;
use App\Modules\SalonWorkforce\Interfaces\Http\Controllers\WorkforceStaffController;
use App\Modules\StoredValue\Interfaces\Http\Controllers\AdminMembershipPlanController;
use App\Modules\StoredValue\Interfaces\Http\Controllers\GiftCardIssueController;
use App\Modules\StoredValue\Interfaces\Http\Controllers\GiftCardLookupController;
use App\Modules\StoredValue\Interfaces\Http\Controllers\GiftCardTopUpController;
use App\Modules\StoredValue\Interfaces\Http\Controllers\MembershipActivateController;
use App\Modules\StoredValue\Interfaces\Http\Controllers\MembershipLookupController;
use App\Platform\Http\Middleware\ApplyAdminRequestContext;
use App\Platform\Http\Middleware\ApplyPosRequestContext;
use App\Platform\Http\Middleware\EnsurePosApiHeaders;
use App\Platform\Http\Middleware\RequireIdempotencyKey;
use Illuminate\Support\Facades\Route;

Route::post('webhooks/fiserv/trans-notify', FiservTransNotifyWebhookController::class)
    ->name('webhooks.fiserv.trans_notify');

Route::prefix('super-admin/v1')
    ->middleware(['auth:sanctum', 'super.admin'])
    ->group(function (): void {
        Route::get('merchants', [SuperAdminMerchantController::class, 'index'])
            ->name('super_admin.merchants.index');
        Route::post('merchants', [SuperAdminMerchantController::class, 'store'])
            ->name('super_admin.merchants.store');
        Route::post('merchants/{merchant}/suspend', [SuperAdminMerchantController::class, 'suspend'])
            ->name('super_admin.merchants.suspend');
        Route::post('merchants/{merchant}/reinstate', [SuperAdminMerchantController::class, 'reinstate'])
            ->name('super_admin.merchants.reinstate');
        Route::get('feature-flags', [SuperAdminFeatureFlagController::class, 'index'])
            ->name('super_admin.feature_flags.index');
        Route::post('feature-flags/{flagKey}', [SuperAdminFeatureFlagController::class, 'upsert'])
            ->name('super_admin.feature_flags.upsert');
        Route::post('device-profiles', [SuperAdminDeviceProfileController::class, 'store'])
            ->name('super_admin.device_profiles.store');
    });

Route::prefix('pos/v{major}')
    ->whereNumber('major')
    ->middleware([EnsurePosApiHeaders::class])
    ->group(function (): void {
        Route::post('auth/enroll', [DeviceAuthController::class, 'enroll'])->name('pos.auth.enroll');
        Route::post('auth/refresh', [DeviceAuthController::class, 'refresh'])->name('pos.auth.refresh');

        Route::middleware([
            'auth:sanctum',
            'abilities:pos:access',
            ApplyPosRequestContext::class,
        ])->group(function (): void {
            Route::post('auth/logout', [DeviceAuthController::class, 'logout'])->name('pos.auth.logout');
            Route::get('bootstrap', PosBootstrapController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.bootstrap');
            Route::get('config', PosConfigController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.config');
            Route::get('restaurant/tables', [DiningTableController::class, 'index'])
                ->middleware('throttle:pos-api')
                ->name('pos.restaurant.tables.index');
            Route::get('customers/search', CustomerSearchController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.customers.search');
            Route::get('gift-cards/lookup', GiftCardLookupController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.gift_cards.lookup');
            Route::get('memberships/lookup', MembershipLookupController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.memberships.lookup');
            Route::get('reports/business-day-summary', BusinessDaySummaryController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.reports.business_day_summary');
            Route::get('payments/{payment}/inquiry', PaymentInquiryController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.payments.inquiry');
            Route::get('exceptions/open', OpenExceptionCaseController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.exceptions.open');
            Route::get('workforce/staff', WorkforceStaffController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.workforce.staff');
            Route::get('workforce/appointments', [AppointmentController::class, 'index'])
                ->middleware('throttle:pos-api')
                ->name('pos.workforce.appointments.index');
            Route::get('workforce/labor-analytics', WorkforceLaborAnalyticsController::class)
                ->middleware('throttle:pos-api')
                ->name('pos.workforce.labor_analytics');
            Route::get('delivery/orders/external', [DeliveryOrderController::class, 'index'])
                ->middleware('throttle:pos-api')
                ->name('pos.delivery.orders.external.index');
            Route::get('retail/inventory/lookup', [RetailInventoryController::class, 'lookup'])
                ->middleware('throttle:pos-api')
                ->name('pos.retail.inventory.lookup');
            Route::get('sync/deltas', [SyncController::class, 'pull'])
                ->middleware('throttle:pos-sync')
                ->name('pos.sync.pull');
            Route::get('sync/recovery-runs/{syncRecoveryRun}', [SyncRecoveryRunController::class, 'show'])
                ->middleware('throttle:pos-sync')
                ->name('pos.sync.recovery_runs.show');

            Route::middleware([
                'throttle:pos-api',
                RequireIdempotencyKey::class,
            ])->group(function (): void {
                Route::post('register-sessions/open', [RegisterSessionController::class, 'open'])->name('pos.register.open');
                Route::post('register-sessions/{registerSession}/close', [RegisterSessionController::class, 'close'])->name('pos.register.close');
                Route::post('orders', [OrderController::class, 'store'])->name('pos.orders.store');
                Route::post('orders/{order}/tenders', [OrderTenderController::class, 'store'])->name('pos.orders.tenders.store');
                Route::post('orders/{order}/cash-checkout', [OrderCashCheckoutController::class, 'store'])->name('pos.orders.cash_checkout');
                Route::post('payments/{payment}/refunds', [PaymentRefundController::class, 'store'])->name('pos.payments.refunds.store');
                Route::post('payments/{payment}/void', [PaymentVoidController::class, 'store'])->name('pos.payments.void');
                Route::post('gift-cards/issue', GiftCardIssueController::class)->name('pos.gift_cards.issue');
                Route::post('gift-cards/top-up', GiftCardTopUpController::class)->name('pos.gift_cards.top_up');
                Route::post('memberships/activate', MembershipActivateController::class)->name('pos.memberships.activate');
                Route::post('restaurant/tables/{diningTable}/claim', [DiningTableController::class, 'claim'])->name('pos.restaurant.tables.claim');
                Route::post('restaurant/tables/{diningTable}/heartbeat', [DiningTableController::class, 'heartbeat'])->name('pos.restaurant.tables.heartbeat');
                Route::post('restaurant/tables/{diningTable}/release', [DiningTableController::class, 'release'])->name('pos.restaurant.tables.release');
                Route::post('workforce/appointments/slot-claims', [AppointmentController::class, 'claimSlot'])->name('pos.workforce.slot_claims.store');
                Route::post('workforce/appointments/slot-claims/{slotClaim}/heartbeat', [AppointmentController::class, 'heartbeatSlot'])->name('pos.workforce.slot_claims.heartbeat');
                Route::post('workforce/appointments/slot-claims/{slotClaim}/release', [AppointmentController::class, 'releaseSlot'])->name('pos.workforce.slot_claims.release');
                Route::post('workforce/appointments', [AppointmentController::class, 'store'])->name('pos.workforce.appointments.store');
                Route::post('workforce/appointments/{appointment}/check-in', [AppointmentController::class, 'checkIn'])->name('pos.workforce.appointments.check_in');
                Route::post('workforce/appointments/{appointment}/complete', [AppointmentController::class, 'complete'])->name('pos.workforce.appointments.complete');
                Route::post('workforce/shifts/open', [ShiftController::class, 'open'])->name('pos.workforce.shifts.open');
                Route::post('workforce/shifts/{shift}/close', [ShiftController::class, 'close'])->name('pos.workforce.shifts.close');
                Route::post('sync/recovery-runs', [SyncRecoveryRunController::class, 'store'])->name('pos.sync.recovery_runs.store');
                Route::post('delivery/orders/external/ingest', [DeliveryOrderController::class, 'ingest'])->name('pos.delivery.orders.external.ingest');
                Route::post('delivery/orders/external/{externalOrderLink}/confirm', [DeliveryOrderController::class, 'confirm'])->name('pos.delivery.orders.external.confirm');
                Route::post('delivery/orders/external/{externalOrderLink}/status', [DeliveryOrderController::class, 'updateStatus'])->name('pos.delivery.orders.external.status');
                Route::post('delivery/orders/external/{externalOrderLink}/cancel', [DeliveryOrderController::class, 'cancel'])->name('pos.delivery.orders.external.cancel');
                Route::post('delivery/store-availability', [DeliveryAvailabilityController::class, 'setStoreAvailability'])->name('pos.delivery.store_availability');
                Route::post('delivery/items/{catalogItem}/availability', [DeliveryAvailabilityController::class, 'setItemAvailability'])->name('pos.delivery.items.availability');
                Route::post('retail/inventory/receive', [RetailInventoryController::class, 'receive'])->name('pos.retail.inventory.receive');
                Route::post('retail/inventory/transfer', [RetailInventoryController::class, 'transfer'])->name('pos.retail.inventory.transfer');
                Route::post('retail/inventory/adjust', [RetailInventoryController::class, 'adjust'])->name('pos.retail.inventory.adjust');
                Route::post('retail/inventory/returns', [RetailInventoryController::class, 'processReturn'])->name('pos.retail.inventory.returns');
                Route::post('device-status/events', [DeviceStatusEventController::class, 'store'])->name('pos.device_status_events.store');
            });

            Route::post('sync/events', [SyncController::class, 'push'])
                ->middleware([
                    'throttle:pos-sync',
                    RequireIdempotencyKey::class,
                ])
                ->name('pos.sync.push');
        });
    });

Route::prefix('admin/v1')
    ->middleware(['auth:sanctum', ApplyAdminRequestContext::class])
    ->group(function (): void {
        Route::get('merchants/{merchant}/stores', [AdminStoreController::class, 'index'])
            ->name('admin.stores.index');
        Route::post('merchants/{merchant}/stores', [AdminStoreController::class, 'store'])
            ->name('admin.stores.store');
        Route::get('device-profiles', [AdminDeviceProfileController::class, 'index'])
            ->name('admin.device_profiles.index');
        Route::get('stores/{store}/orders', [AdminOrderController::class, 'index'])
            ->name('admin.orders.index');
        Route::get('stores/{store}/orders/{order}', [AdminOrderController::class, 'show'])
            ->name('admin.orders.show');
        Route::get('stores/{store}/users', [AdminStoreUserController::class, 'index'])
            ->name('admin.users.index');
        Route::get('stores/{store}/roles', [AdminStoreUserController::class, 'roles'])
            ->name('admin.users.roles');
        Route::post('stores/{store}/users/invitations', [AdminStoreUserController::class, 'invite'])
            ->name('admin.users.invite');
        Route::post('stores/{store}/users/{user}/roles', [AdminStoreUserController::class, 'assignRole'])
            ->name('admin.users.roles.assign');
        Route::delete('stores/{store}/users/{user}/roles/{role}', [AdminStoreUserController::class, 'revokeRole'])
            ->name('admin.users.roles.revoke');
        Route::get('stores/{store}/devices', [AdminDeviceController::class, 'index'])
            ->name('admin.devices.index');
        Route::post('stores/{store}/devices/{device}/deactivate', [AdminDeviceController::class, 'deactivate'])
            ->name('admin.devices.deactivate');
        Route::post('stores/{store}/device-enrollment-codes', DeviceEnrollmentCodeController::class)
            ->name('admin.device_enrollment_codes.store');
        Route::post('stores/{store}/feature-flags/{flagKey}', StoreFeatureFlagController::class)
            ->name('admin.feature_flags.upsert');
        Route::post('stores/{store}/customers', AdminCustomerController::class)
            ->name('admin.customers.store');
        Route::post('stores/{store}/customers/{customer}/privacy-export', [AdminCustomerPrivacyController::class, 'export'])
            ->name('admin.customers.privacy_export');
        Route::post('stores/{store}/customers/{customer}/tombstone', [AdminCustomerPrivacyController::class, 'tombstone'])
            ->name('admin.customers.tombstone');
        Route::post('merchants/{merchant}/privacy-export', [AdminMerchantPrivacyController::class, 'export'])
            ->name('admin.merchants.privacy_export');
        Route::post('stores/{store}/discount-rules', AdminDiscountRuleController::class)
            ->name('admin.discount_rules.store');
        Route::get('stores/{store}/catalog/items', [AdminCatalogItemController::class, 'index'])
            ->name('admin.catalog.items.index');
        Route::post('stores/{store}/catalog/items', [AdminCatalogItemController::class, 'store'])
            ->name('admin.catalog.items.store');
        Route::put('stores/{store}/catalog/items/{catalogItem}', [AdminCatalogItemController::class, 'update'])
            ->name('admin.catalog.items.update');
        Route::post('stores/{store}/catalog/items/{catalogItem}/deactivate', [AdminCatalogItemController::class, 'deactivate'])
            ->name('admin.catalog.items.deactivate');
        Route::get('stores/{store}/catalog/categories', [AdminCategoryController::class, 'index'])
            ->name('admin.catalog.categories.index');
        Route::post('stores/{store}/catalog/categories', [AdminCategoryController::class, 'store'])
            ->name('admin.catalog.categories.store');
        Route::put('stores/{store}/catalog/categories/{category}', [AdminCategoryController::class, 'update'])
            ->name('admin.catalog.categories.update');
        Route::post('stores/{store}/catalog/categories/{category}/deactivate', [AdminCategoryController::class, 'deactivate'])
            ->name('admin.catalog.categories.deactivate');
        Route::post('stores/{store}/catalog/variants', [AdminVariantController::class, 'store'])
            ->name('admin.catalog.variants.store');
        Route::put('stores/{store}/catalog/variants/{variant}', [AdminVariantController::class, 'update'])
            ->name('admin.catalog.variants.update');
        Route::post('stores/{store}/catalog/variants/{variant}/deactivate', [AdminVariantController::class, 'deactivate'])
            ->name('admin.catalog.variants.deactivate');
        Route::post('stores/{store}/catalog/modifier-groups', [AdminModifierGroupController::class, 'store'])
            ->name('admin.catalog.modifier_groups.store');
        Route::put('stores/{store}/catalog/modifier-groups/{modifierGroup}', [AdminModifierGroupController::class, 'update'])
            ->name('admin.catalog.modifier_groups.update');
        Route::post('stores/{store}/catalog/modifier-groups/{modifierGroup}/deactivate', [AdminModifierGroupController::class, 'deactivate'])
            ->name('admin.catalog.modifier_groups.deactivate');
        Route::post('stores/{store}/catalog/modifier-options', [AdminModifierOptionController::class, 'store'])
            ->name('admin.catalog.modifier_options.store');
        Route::put('stores/{store}/catalog/modifier-options/{modifierOption}', [AdminModifierOptionController::class, 'update'])
            ->name('admin.catalog.modifier_options.update');
        Route::post('stores/{store}/catalog/modifier-options/{modifierOption}/deactivate', [AdminModifierOptionController::class, 'deactivate'])
            ->name('admin.catalog.modifier_options.deactivate');
        Route::post('stores/{store}/catalog/combo-packages', [AdminComboPackageController::class, 'store'])
            ->name('admin.catalog.combo_packages.store');
        Route::put('stores/{store}/catalog/combo-packages/{comboPackage}', [AdminComboPackageController::class, 'update'])
            ->name('admin.catalog.combo_packages.update');
        Route::post('stores/{store}/catalog/combo-packages/{comboPackage}/deactivate', [AdminComboPackageController::class, 'deactivate'])
            ->name('admin.catalog.combo_packages.deactivate');
        Route::post('stores/{store}/catalog/items/{catalogItem}/add-ons', [AdminCatalogItemAddOnController::class, 'store'])
            ->name('admin.catalog.item_add_ons.store');
        Route::post('stores/{store}/membership-plans', AdminMembershipPlanController::class)
            ->name('admin.membership_plans.store');
        Route::post('stores/{store}/dining-tables', AdminDiningTableController::class)
            ->name('admin.dining_tables.store');
        Route::post('stores/{store}/printer-configs', AdminPrinterConfigController::class)
            ->name('admin.printer_configs.store');
        Route::post('stores/{store}/print-routes', AdminPrintRouteController::class)
            ->name('admin.print_routes.store');
        Route::post('stores/{store}/workforce/service-items', AdminServiceItemController::class)
            ->name('admin.workforce.service_items.store');
        Route::post('stores/{store}/workforce/staff-profiles', AdminStaffProfileController::class)
            ->name('admin.workforce.staff_profiles.store');
        Route::post('stores/{store}/workforce/commission-rules', AdminCommissionRuleController::class)
            ->name('admin.workforce.commission_rules.store');
        Route::post('stores/{store}/workforce/wage-rules', AdminWageRuleController::class)
            ->name('admin.workforce.wage_rules.store');
        Route::post('stores/{store}/workforce/staff-service-rules', AdminStaffServiceRuleController::class)
            ->name('admin.workforce.staff_service_rules.upsert');
        Route::post('stores/{store}/workforce/payroll-snapshots/generate', AdminGeneratePayrollSnapshotController::class)
            ->name('admin.workforce.payroll_snapshots.generate');
        Route::get('stores/{store}/workforce/payroll-snapshots/{payrollSnapshot}', [AdminPayrollSnapshotController::class, 'show'])
            ->name('admin.workforce.payroll_snapshots.show');
        Route::get('stores/{store}/workforce/labor-analytics', AdminLaborAnalyticsController::class)
            ->name('admin.workforce.labor_analytics');
        Route::post('stores/{store}/workforce/attendance-records/{attendanceRecord}/approve', AdminApproveAttendanceRecordController::class)
            ->name('admin.workforce.attendance_records.approve');
        Route::post('stores/{store}/delivery/channel-configs', [AdminDeliveryChannelConfigController::class, 'store'])
            ->name('admin.delivery.channel_configs.store');
        Route::post('stores/{store}/delivery/channel-configs/{deliveryChannelConfig}/publish-menu', [AdminDeliveryChannelConfigController::class, 'publishMenuNow'])
            ->name('admin.delivery.channel_configs.publish_menu');
        Route::post('stores/{store}/delivery/channel-configs/{deliveryChannelConfig}/disconnect', [AdminDeliveryChannelConfigController::class, 'disconnect'])
            ->name('admin.delivery.channel_configs.disconnect');
        Route::post('stores/{store}/delivery/channel-configs/{deliveryChannelConfig}/store-availability', [AdminDeliveryChannelConfigController::class, 'setStoreAvailability'])
            ->name('admin.delivery.channel_configs.store_availability');
        Route::post('stores/{store}/retail/barcode-records', [AdminBarcodeRecordController::class, 'store'])
            ->name('admin.retail.barcode_records.store');
        Route::post('stores/{store}/retail/promotions', [AdminRetailPromotionController::class, 'store'])
            ->name('admin.retail.promotions.store');
        Route::put('stores/{store}/retail/promotions/{discountRule}', [AdminRetailPromotionController::class, 'update'])
            ->name('admin.retail.promotions.update');
        Route::post('stores/{store}/retail/promotions/{discountRule}/activate', [AdminRetailPromotionController::class, 'activate'])
            ->name('admin.retail.promotions.activate');
        Route::post('stores/{store}/retail/promotions/{discountRule}/deactivate', [AdminRetailPromotionController::class, 'deactivate'])
            ->name('admin.retail.promotions.deactivate');
        Route::get('stores/{store}/reports/delivery-health', AdminDeliveryOperationalHealthController::class)
            ->name('admin.reports.delivery_health');
        Route::get('stores/{store}/reports/retail-stock-movements', AdminRetailStockMovementSummaryController::class)
            ->name('admin.reports.retail_stock_movements');
        Route::get('stores/{store}/archive/receipts/{receipt}', AdminArchivedReceiptController::class)
            ->name('admin.archive.receipts.show');
        Route::get('stores/{store}/archive/audit-logs/{auditLog}', AdminArchivedAuditLogController::class)
            ->name('admin.archive.audit_logs.show');
        Route::get('stores/{store}/archive/payroll-snapshots/{payrollSnapshot}', AdminArchivedPayrollSnapshotController::class)
            ->name('admin.archive.payroll_snapshots.show');
        Route::post('exception-cases/{exceptionCase}/resolve', AdminResolveExceptionCaseController::class)
            ->name('admin.exception_cases.resolve');
    });
