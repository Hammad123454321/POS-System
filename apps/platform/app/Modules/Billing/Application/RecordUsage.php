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
        ?string $sourceRef = null,
    ): ?UsageRecord {
        if (! config('pos.billing.enabled', true)) {
            return null;
        }

        // When a source_ref is provided, make the write idempotent so queued
        // listener retries cannot double-meter the same business event.
        if ($sourceRef !== null) {
            $existing = UsageRecord::query()
                ->where('metric_key', $metricKey)
                ->where('source_ref', $sourceRef)
                ->first();

            if ($existing !== null) {
                return $existing;
            }
        }

        return UsageRecord::query()->create([
            'merchant_id' => $merchantId,
            'store_id' => $storeId,
            'metric_key' => $metricKey,
            'source_ref' => $sourceRef,
            'quantity' => $quantity,
            'recorded_on' => CarbonImmutable::now('UTC')->toDateString(),
            'metadata' => $metadata,
        ]);
    }
}
