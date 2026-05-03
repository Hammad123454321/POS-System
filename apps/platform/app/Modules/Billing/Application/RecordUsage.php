<?php

namespace App\Modules\Billing\Application;

use App\Modules\Billing\Domain\Models\UsageRecord;
use Carbon\CarbonImmutable;

class RecordUsage
{
    /**
     * @param  array<string, mixed>|null  $metadata
     */
    public function handle(
        ?string $merchantId,
        ?string $storeId,
        string $metricKey,
        int $quantity = 1,
        ?array $metadata = null,
    ): ?UsageRecord {
        if (! config('pos.billing.enabled', true)) {
            return null;
        }

        return UsageRecord::query()->create([
            'merchant_id' => $merchantId,
            'store_id' => $storeId,
            'metric_key' => $metricKey,
            'quantity' => $quantity,
            'recorded_on' => CarbonImmutable::now('UTC')->toDateString(),
            'metadata' => $metadata,
        ]);
    }
}
