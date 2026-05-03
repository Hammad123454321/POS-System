<?php

namespace App\Modules\StoredValue\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Domain\Models\MembershipPlan;

class MembershipPlanSnapshotQuery
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function forDevice(Device $device): array
    {
        return MembershipPlan::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('is_active', true)
            ->orderBy('name')
            ->get()
            ->map(fn (MembershipPlan $plan): array => [
                'id' => $plan->id,
                'name' => $plan->name,
                'code' => $plan->code,
                'price_minor' => $plan->price_minor,
                'currency' => $plan->currency,
                'duration_days' => $plan->duration_days,
            ])->values()->all();
    }
}
