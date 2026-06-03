<?php

namespace App\Modules\Reporting\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Domain\Models\InventoryAdjustment;
use App\Platform\Support\Reporting\ReportingConnection;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;

class RetailStockMovementSummaryQuery
{
    public function __construct(
        private readonly ReportingConnection $reportingConnection,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Store $store, ?string $from = null, ?string $to = null): array
    {
        $fromAt = $from ? CarbonImmutable::parse($from, 'UTC') : CarbonImmutable::now('UTC')->subDay();
        $toAt = $to ? CarbonImmutable::parse($to, 'UTC') : CarbonImmutable::now('UTC');

        $movements = $this->reportingConnection->query(InventoryAdjustment::class)
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereBetween('created_at', [$fromAt, $toAt]);

        $byType = (clone $movements)
            ->select('adjustment_type', DB::raw('SUM(quantity_delta) as quantity_delta_total'))
            ->groupBy('adjustment_type')
            ->orderBy('adjustment_type')
            ->get()
            ->map(fn ($row): array => [
                'adjustment_type' => (string) $row->adjustment_type,
                'quantity_delta_total' => (int) $row->quantity_delta_total,
            ])
            ->values()
            ->all();

        return [
            'from' => $fromAt->toIso8601String(),
            'to' => $toAt->toIso8601String(),
            'movement_count' => (clone $movements)->count(),
            'net_quantity_delta' => (int) (clone $movements)->sum('quantity_delta'),
            'by_adjustment_type' => $byType,
        ];
    }
}
