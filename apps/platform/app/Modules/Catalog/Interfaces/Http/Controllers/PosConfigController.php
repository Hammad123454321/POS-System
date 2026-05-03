<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Application\Queries\CatalogSnapshotQuery;
use App\Modules\Catalog\Application\Queries\DiscountRuleSnapshotQuery;
use App\Modules\DeliveryIntegrations\Application\Queries\DeliveryChannelConfigSnapshotQuery;
use App\Modules\Payments\Application\Queries\PaymentCapabilityQuery;
use App\Modules\PlatformCore\Application\Features\FeatureFlagSnapshotQuery;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Restaurant\Application\Queries\PrintRouteSnapshotQuery;
use App\Modules\Retail\Application\Queries\RetailConfigSnapshotQuery;
use App\Modules\Retail\Application\Queries\RetailPromotionSnapshotQuery;
use App\Modules\SalonWorkforce\Application\Queries\SalonWorkforceConfigSnapshotQuery;
use App\Modules\StoredValue\Application\Queries\MembershipPlanSnapshotQuery;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class PosConfigController extends Controller
{
    public function __invoke(
        Request $request,
        CatalogSnapshotQuery $catalogQuery,
        DiscountRuleSnapshotQuery $discountQuery,
        MembershipPlanSnapshotQuery $membershipPlanQuery,
        PaymentCapabilityQuery $paymentCapabilityQuery,
        PrintRouteSnapshotQuery $printRouteQuery,
        FeatureFlagSnapshotQuery $featureFlagSnapshotQuery,
        SalonWorkforceConfigSnapshotQuery $salonWorkforceConfigSnapshotQuery,
        DeliveryChannelConfigSnapshotQuery $deliveryChannelConfigSnapshotQuery,
        RetailConfigSnapshotQuery $retailConfigSnapshotQuery,
        RetailPromotionSnapshotQuery $retailPromotionSnapshotQuery,
    ): JsonResponse {
        /** @var Device $device */
        $device = $request->user();

        return response()->json([
            ...$catalogQuery->forDevice($device),
            'discount_rules' => $discountQuery->forDevice($device),
            'membership_plans' => $membershipPlanQuery->forDevice($device),
            'payment_capabilities' => $paymentCapabilityQuery->forDevice($device),
            'print_routes' => $printRouteQuery->forDevice($device),
            'feature_flags' => $featureFlagSnapshotQuery->forDevice($device),
            'delivery_channels' => $deliveryChannelConfigSnapshotQuery->forDevice($device),
            'retail_promotions' => $retailPromotionSnapshotQuery->forDevice($device),
            ...$retailConfigSnapshotQuery->forDevice($device),
            ...$salonWorkforceConfigSnapshotQuery->forDevice($device),
        ]);
    }
}
