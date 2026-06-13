<?php

namespace App\Modules\Reporting\Application\Actions;

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Reporting\Domain\Models\ReportDailyStoreSummary;
use Carbon\CarbonImmutable;

/**
 * Materializes one day's paid-order aggregates for a store into
 * report_daily_store_summaries. Writes always target the primary connection
 * (the read replica is read-only and receives this via replication).
 */
class RefreshDailyStoreSummary
{
    public function handle(Store $store, string $businessDate, bool $final = false): ReportDailyStoreSummary
    {
        $paidOrders = Order::query()
            ->where('store_id', $store->id)
            ->where('business_date', $businessDate)
            ->where('status', 'paid');

        $ordersCount = (clone $paidOrders)->count();
        $gross = (int) (clone $paidOrders)->sum('total_minor');
        $tax = (int) (clone $paidOrders)->sum('tax_minor');
        $discount = (int) (clone $paidOrders)->sum('discount_minor');
        $net = $gross - $tax;

        $tenderBreakdown = Payment::query()
            ->where('store_id', $store->id)
            ->where('status', 'captured')
            ->whereHas('order', fn ($q) => $q->where('business_date', $businessDate)->where('status', 'paid'))
            ->selectRaw('method, SUM(amount_minor) AS total')
            ->groupBy('method')
            ->pluck('total', 'method')
            ->map(fn ($v) => (int) $v)
            ->all();

        return ReportDailyStoreSummary::query()->updateOrCreate(
            ['store_id' => $store->id, 'business_date' => $businessDate],
            [
                'merchant_id' => $store->merchant_id,
                'orders_count' => $ordersCount,
                'gross_minor' => $gross,
                'tax_minor' => $tax,
                'discount_minor' => $discount,
                'net_minor' => $net,
                'tender_breakdown' => $tenderBreakdown,
                'last_aggregated_at' => CarbonImmutable::now('UTC'),
                'is_final' => $final,
            ],
        );
    }
}
