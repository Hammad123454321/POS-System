<?php

namespace App\Modules\Reporting\Application\Queries;

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Reporting\Domain\Models\ReportDailyStoreSummary;
use App\Platform\Support\Reporting\ReportingConnection;
use App\Platform\Support\Time\BusinessClock;
use Carbon\CarbonImmutable;

class BusinessDaySummaryQuery
{
    public function __construct(
        private readonly BusinessClock $businessClock,
        private readonly ReportingConnection $reportingConnection,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device): array
    {
        $store = $device->store()->firstOrFail();
        $activeSession = $this->reportingConnection->query(RegisterSession::class)
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('status', 'open')
            ->latest('opened_at')
            ->first();
        $businessDate = $activeSession?->business_date?->format('Y-m-d')
            ?? $this->businessClock->businessDateForStore($store);

        // Prefer a materialized summary for the heavy paid-order aggregates when
        // one is fresh (or finalized); otherwise compute live.
        $summary = $this->freshSummary($device->merchant_id, $device->store_id, $businessDate);

        $paidOrdersCount = $summary?->orders_count ?? $this->reportingConnection->query(Order::class)
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('business_date', $businessDate)
            ->where('status', 'paid')
            ->count();

        $grossSalesMinor = $summary !== null ? (int) $summary->gross_minor : (int) $this->reportingConnection->query(Order::class)
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('business_date', $businessDate)
            ->where('status', 'paid')
            ->sum('total_minor');

        return [
            'business_date' => $businessDate,
            'source' => $summary !== null ? 'summary' : 'live',
            'open_orders_count' => $this->reportingConnection->query(Order::class)
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('business_date', $businessDate)
                ->where('status', 'open')
                ->count(),
            'paid_orders_count' => $paidOrdersCount,
            'gross_sales_minor' => $grossSalesMinor,
            'cash_sales_minor' => (int) $this->reportingConnection->query(Payment::class)
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('method', 'cash')
                ->where('status', 'captured')
                ->whereHas('order', fn ($query) => $query->where('business_date', $businessDate))
                ->sum('amount_minor'),
            'open_register_sessions_count' => $this->reportingConnection->query(RegisterSession::class)
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('business_date', $businessDate)
                ->where('status', 'open')
                ->count(),
            'open_exception_cases_count' => $this->reportingConnection->query(ExceptionCase::class)
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('status', 'open')
                ->count(),
        ];
    }

    private function freshSummary(string $merchantId, string $storeId, string $businessDate): ?ReportDailyStoreSummary
    {
        /** @var ReportDailyStoreSummary|null $summary */
        $summary = $this->reportingConnection->query(ReportDailyStoreSummary::class)
            ->where('merchant_id', $merchantId)
            ->where('store_id', $storeId)
            ->where('business_date', $businessDate)
            ->first();

        if ($summary === null) {
            return null;
        }

        if ($summary->is_final) {
            return $summary;
        }

        $stalenessSeconds = (int) config('pos.reporting.summary_staleness_seconds', 300);
        $aggregatedAt = $summary->last_aggregated_at;

        if ($aggregatedAt === null) {
            return null;
        }

        return $aggregatedAt->gte(CarbonImmutable::now('UTC')->subSeconds($stalenessSeconds))
            ? $summary
            : null;
    }
}
