<?php

namespace App\Modules\Catalog\Application\Queries;

use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;

class DiscountRuleSnapshotQuery
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function forDevice(Device $device): array
    {
        $now = CarbonImmutable::now('UTC');

        return DiscountRule::query()
            ->forMerchant($device->merchant_id)
            ->where('is_active', true)
            ->where(function ($query) use ($device): void {
                $query->whereNull('store_id')
                    ->orWhere('store_id', $device->store_id);
            })
            ->where(function ($query) use ($now): void {
                $query->whereNull('starts_at')
                    ->orWhere('starts_at', '<=', $now);
            })
            ->where(function ($query) use ($now): void {
                $query->whereNull('ends_at')
                    ->orWhere('ends_at', '>=', $now);
            })
            ->orderBy('sort_order')
            ->orderBy('name')
            ->get()
            ->map(fn (DiscountRule $rule): array => [
                'id' => $rule->id,
                'name' => $rule->name,
                'code' => $rule->code,
                'type' => $rule->type,
                'value_minor' => $rule->value_minor,
                'value_basis_points' => $rule->value_basis_points,
                'scope_mode' => $rule->scope_mode,
                'applicability' => $rule->applicability,
                'is_stackable' => $rule->is_stackable,
                'starts_at' => $rule->starts_at?->toIso8601String(),
                'ends_at' => $rule->ends_at?->toIso8601String(),
            ])
            ->values()
            ->all();
    }
}
