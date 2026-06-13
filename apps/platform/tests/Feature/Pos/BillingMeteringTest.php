<?php

use App\Modules\Billing\Application\Listeners\RecordOrderPaidUsage;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\Billing\Domain\Models\UsageRecord;
use App\Modules\OrderRegister\Domain\Events\OrderPaid;
use Illuminate\Support\Facades\Event;
use Laravel\Sanctum\Sanctum;

function payOrder(string $idemSuffix): string
{
    [$device, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

    $orderId = test()->withHeaders(posHeaders() + ['Idempotency-Key' => "bm-o-{$idemSuffix}"])
        ->postJson('/api/pos/v1/orders', [
            'register_session_id' => $session->id,
            'lines' => [['catalog_item_id' => $item->id, 'quantity' => 1]],
        ])->json('data.id');

    test()->withHeaders(posHeaders() + ['Idempotency-Key' => "bm-co-{$idemSuffix}"])
        ->postJson("/api/pos/v1/orders/{$orderId}/cash-checkout", ['tendered_minor' => 5000])
        ->assertCreated();

    return $orderId;
}

it('dispatches OrderPaid when an order is paid', function () {
    Event::fake([OrderPaid::class]);

    $orderId = payOrder('a');

    Event::assertDispatched(OrderPaid::class, fn ($e) => $e->orderId === $orderId);
});

it('records a single orders.paid usage row via the listener', function () {
    $orderId = payOrder('b');

    expect(UsageRecord::query()->where('metric_key', 'orders.paid')->where('source_ref', $orderId)->count())->toBe(1);
});

it('is idempotent if the listener runs twice for the same order', function () {
    $orderId = payOrder('c');

    // Re-run the listener manually with the same event.
    app(RecordOrderPaidUsage::class)->handle(new OrderPaid($orderId, null, null, 1100));

    expect(UsageRecord::query()->where('metric_key', 'orders.paid')->where('source_ref', $orderId)->count())->toBe(1);
});

it('keeps source_ref idempotency only when provided', function () {
    $usage = app(RecordUsage::class);
    $usage->handle(null, null, 'manual.metric', 1);
    $usage->handle(null, null, 'manual.metric', 1);

    expect(UsageRecord::query()->where('metric_key', 'manual.metric')->count())->toBe(2);
});
