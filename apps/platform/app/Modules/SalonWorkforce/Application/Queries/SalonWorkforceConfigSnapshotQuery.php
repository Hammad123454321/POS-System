<?php

namespace App\Modules\SalonWorkforce\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\ServiceItem;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use App\Modules\SalonWorkforce\Domain\Models\StaffServiceRule;
use App\Modules\SalonWorkforce\Domain\Models\WageRule;

class SalonWorkforceConfigSnapshotQuery
{
    /**
     * @return array<string, mixed>
     */
    public function forDevice(Device $device): array
    {
        $serviceItems = ServiceItem::query()
            ->where('merchant_id', $device->merchant_id)
            ->with('catalogItem')
            ->orderByDesc('created_at')
            ->get();

        $staffProfiles = StaffProfile::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('status', 'active')
            ->orderBy('display_name')
            ->get();

        $assignments = StaffServiceRule::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('is_active', true)
            ->with('commissionRule')
            ->get();

        $wageRules = WageRule::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('wage_type', 'hourly')
            ->where('is_active', true)
            ->orderByDesc('effective_from')
            ->get();

        return [
            'salon_services' => $serviceItems->map(fn (ServiceItem $service): array => [
                'id' => $service->id,
                'catalog_item_id' => $service->catalog_item_id,
                'name' => $service->catalogItem?->name,
                'sku' => $service->catalogItem?->sku,
                'base_price_minor' => $service->catalogItem?->base_price_minor,
                'currency' => $service->catalogItem?->currency,
                'duration_minutes' => $service->duration_minutes,
                'buffer_minutes' => $service->buffer_minutes,
                'is_walk_in_enabled' => $service->is_walk_in_enabled,
            ])->values()->all(),
            'staff_profiles' => $staffProfiles->map(fn (StaffProfile $staff): array => [
                'id' => $staff->id,
                'display_name' => $staff->display_name,
                'code' => $staff->code,
                'role_title' => $staff->role_title,
                'status' => $staff->status,
            ])->values()->all(),
            'staff_service_rules' => $assignments->map(fn (StaffServiceRule $assignment): array => [
                'id' => $assignment->id,
                'staff_profile_id' => $assignment->staff_profile_id,
                'service_item_id' => $assignment->service_item_id,
                'commission_rule' => $assignment->commissionRule === null ? null : [
                    'id' => $assignment->commissionRule->id,
                    'base_type' => $assignment->commissionRule->base_type,
                    'rate_basis_points' => $assignment->commissionRule->rate_basis_points,
                    'fixed_minor' => $assignment->commissionRule->fixed_minor,
                    'currency' => $assignment->commissionRule->currency,
                ],
            ])->values()->all(),
            'wage_rules' => $wageRules->map(fn (WageRule $rule): array => [
                'id' => $rule->id,
                'staff_profile_id' => $rule->staff_profile_id,
                'wage_type' => $rule->wage_type,
                'hourly_rate_minor' => $rule->hourly_rate_minor,
                'currency' => $rule->currency,
                'effective_from' => $rule->effective_from?->format('Y-m-d'),
                'effective_to' => $rule->effective_to?->format('Y-m-d'),
            ])->values()->all(),
        ];
    }
}
