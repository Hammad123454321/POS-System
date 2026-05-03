<?php

use App\Modules\DeliveryIntegrations\Application\DeliveryAdapterRegistry;
use App\Modules\DeliveryIntegrations\Application\Jobs\PushDeliveryUpdateJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use Laravel\Sanctum\Sanctum;

it('keeps idempotent final state on duplicate delivery replay for the same external order', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'is_enabled' => true,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $payload = [
        'channel_key' => 'aggregator',
        'external_order_id' => 'dup-ext-order-1',
        'lines' => [
            ['sku' => $item->sku, 'quantity' => 1],
        ],
    ];

    $first = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-chaos-dup-1',
    ]))->postJson('/api/pos/v1/delivery/orders/external/ingest', $payload)->assertCreated()->json('data');

    $second = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-chaos-dup-2',
    ]))->postJson('/api/pos/v1/delivery/orders/external/ingest', $payload)->assertCreated()->json('data');

    expect($first['id'])->toBe($second['id']);
    expect(ExternalOrderLink::query()->where('external_order_id', 'dup-ext-order-1')->count())->toBe(1);
    expect(Order::query()->whereKey($first['order_id'])->exists())->toBeTrue();
});

it('ignores reordered delivery status events and opens an exception case', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'is_enabled' => true,
    ]);

    $external = ExternalOrderLink::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'external_order_id' => 'reordered-1',
        'status' => 'accepted',
        'received_at' => now(),
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-chaos-order-1',
    ]))->postJson("/api/pos/v1/delivery/orders/external/{$external->id}/status", [
        'status' => 'completed',
    ])->assertOk()
        ->assertJsonPath('data.status', 'completed');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-chaos-order-2',
    ]))->postJson("/api/pos/v1/delivery/orders/external/{$external->id}/status", [
        'status' => 'preparing',
    ])->assertOk()
        ->assertJsonPath('data.status', 'completed');

    expect(ExceptionCase::query()
        ->where('merchant_id', $device->merchant_id)
        ->where('store_id', $device->store_id)
        ->where('module', 'delivery')
        ->where('code', 'state_mismatch')
        ->count())->toBe(1);
});

it('creates delivery exceptions on partial channel outage and keeps data consistent', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    $config = DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'uber_eats',
        'is_enabled' => true,
    ]);

    $job = new PushDeliveryUpdateJob(
        deliveryChannelConfigId: $config->id,
        operation: 'update_status',
        payload: [
            'external_order_id' => 'outage-1',
            'status' => 'unknown_status',
        ],
    );

    try {
        $job->handle(
            app(DeliveryAdapterRegistry::class),
            app(OpenExceptionCase::class),
        );
    } catch (Throwable) {
        // Expected path for simulated channel outage / bad provider mapping.
    }

    expect(ExceptionCase::query()
        ->where('merchant_id', $device->merchant_id)
        ->where('store_id', $device->store_id)
        ->where('module', 'delivery')
        ->where('code', 'channel_operation_failed')
        ->count())->toBe(1);
});

it('keeps payments and register flow responsive during delivery queue stress', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    $config = DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'is_enabled' => true,
    ]);

    for ($i = 0; $i < 100; $i++) {
        PushDeliveryUpdateJob::dispatch(
            $config->id,
            'set_item_availability',
            [
                'catalog_item_id' => $item->id,
                'sku' => $item->sku,
                'sold_out' => false,
            ],
        )->onConnection((string) config('pos.delivery.queue_connection', config('queue.default')))
            ->onQueue((string) config('pos.delivery.queue', 'delivery'));
    }

    Sanctum::actingAs($device, ['pos:access']);

    $orderId = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-chaos-stress-order',
    ]))->postJson('/api/pos/v1/orders', [
        'register_session_id' => $registerSession->id,
        'lines' => [
            ['catalog_item_id' => $item->id, 'quantity' => 1],
        ],
    ])->assertCreated()
        ->json('data.id');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-chaos-stress-cash-checkout',
    ]))->postJson("/api/pos/v1/orders/{$orderId}/cash-checkout", [
        'tendered_minor' => 2000,
    ])->assertCreated();

    expect(Order::query()->findOrFail($orderId)->status)->toBe('paid');
});
