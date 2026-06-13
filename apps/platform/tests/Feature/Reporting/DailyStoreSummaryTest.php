<?php

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Reporting\Application\Actions\RefreshDailyStoreSummary;
use App\Modules\Reporting\Application\Queries\BusinessDaySummaryQuery;

function makePaidOrder($device, $session, string $businessDate, int $total): Order
{
    return Order::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'register_session_id' => $session->id,
        'device_id' => $device->id,
        'order_number' => 'S-'.uniqid(),
        'status' => 'paid',
        'business_date' => $businessDate,
        'currency' => 'USD',
        'subtotal_minor' => $total - 100,
        'tax_minor' => 100,
        'discount_minor' => 0,
        'total_minor' => $total,
        'paid_minor' => $total,
        'opened_at' => now(),
    ]);
}

it('materializes a summary that matches live aggregation', function () {
    [$device, $session, $item] = buildPosOrderContext();
    $store = Store::query()->find($device->store_id);

    makePaidOrder($device, $session, '2026-05-01', 1100);
    makePaidOrder($device, $session, '2026-05-01', 2100);

    $summary = app(RefreshDailyStoreSummary::class)->handle($store, '2026-05-01');

    expect($summary->orders_count)->toBe(2);
    expect((int) $summary->gross_minor)->toBe(3200);
    expect((int) $summary->tax_minor)->toBe(200);
});

it('serves a fresh summary from the business-day query', function () {
    [$device, $session, $item] = buildPosOrderContext();
    $store = Store::query()->find($device->store_id);

    makePaidOrder($device, $session, $session->business_date->format('Y-m-d'), 5000);

    app(RefreshDailyStoreSummary::class)->handle($store, $session->business_date->format('Y-m-d'));

    $result = app(BusinessDaySummaryQuery::class)->handle($device->fresh());

    expect($result['source'])->toBe('summary');
    expect($result['gross_sales_minor'])->toBe(5000);
});

it('falls back to live aggregation when no summary exists', function () {
    [$device, $session, $item] = buildPosOrderContext();
    makePaidOrder($device, $session, $session->business_date->format('Y-m-d'), 4200);

    $result = app(BusinessDaySummaryQuery::class)->handle($device->fresh());

    expect($result['source'])->toBe('live');
    expect($result['gross_sales_minor'])->toBe(4200);
});

it('ignores a stale non-final summary and recomputes live', function () {
    [$device, $session, $item] = buildPosOrderContext();
    $store = Store::query()->find($device->store_id);
    $bd = $session->business_date->format('Y-m-d');

    makePaidOrder($device, $session, $bd, 9000);
    config()->set('pos.reporting.summary_staleness_seconds', 60);

    $summary = app(RefreshDailyStoreSummary::class)->handle($store, $bd);
    // Force it stale.
    $summary->forceFill(['last_aggregated_at' => now()->subMinutes(10), 'is_final' => false])->save();

    $result = app(BusinessDaySummaryQuery::class)->handle($device->fresh());

    expect($result['source'])->toBe('live');
});
