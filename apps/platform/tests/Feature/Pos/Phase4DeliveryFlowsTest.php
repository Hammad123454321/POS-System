<?php

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\DeliveryIntegrations\Application\Jobs\PushDeliveryUpdateJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use Laravel\Sanctum\Sanctum;

it('ingests external delivery orders through aggregator and links to internal orders', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'is_enabled' => true,
        'credentials' => ['api_key' => 'test'],
        'mapping' => ['store_external_id' => 'ext-store-1'],
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $result = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-delivery-ingest',
    ]))->postJson('/api/pos/v1/delivery/orders/external/ingest', [
        'channel_key' => 'aggregator',
        'external_order_id' => 'ext-order-1001',
        'lines' => [
            [
                'sku' => $item->sku,
                'quantity' => 2,
            ],
        ],
    ])->assertCreated()
        ->json('data');

    expect($result['channel_key'])->toBe('aggregator');
    expect($result['order_id'])->not->toBeNull();
    expect(Order::query()->whereKey($result['order_id'])->exists())->toBeTrue();
});

it('opens exception case on delivery ingest when external lines do not map to catalog', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'is_enabled' => true,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $result = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-delivery-ingest-unmapped',
    ]))->postJson('/api/pos/v1/delivery/orders/external/ingest', [
        'channel_key' => 'aggregator',
        'external_order_id' => 'ext-order-1002',
        'lines' => [
            [
                'sku' => 'DOES-NOT-EXIST',
                'quantity' => 1,
            ],
        ],
    ])->assertCreated()
        ->json('data');

    expect($result['order_id'] ?? null)->toBeNull();
    expect(ExceptionCase::query()
        ->where('merchant_id', $device->merchant_id)
        ->where('store_id', $device->store_id)
        ->where('module', 'delivery')
        ->where('code', 'external_order_mapping_failed')
        ->count())->toBe(1);
});

it('updates delivery order status and queues delivery operations on isolated queue', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'is_enabled' => true,
    ]);

    /** @var ExternalOrderLink $externalLink */
    $externalLink = ExternalOrderLink::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'external_order_id' => 'ext-order-1003',
        'status' => 'received',
        'received_at' => now(),
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-delivery-status-update',
    ]))->postJson("/api/pos/v1/delivery/orders/external/{$externalLink->id}/status", [
        'status' => 'preparing',
    ])->assertOk()
        ->assertJsonPath('data.status', 'preparing');

    $job = new PushDeliveryUpdateJob('cfg-id', 'update_status', ['external_order_id' => 'ext-order-1003']);
    expect($job->operation)->toBe('update_status');
});

it('propagates sold-out item availability via delivery queue', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'is_enabled' => true,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-delivery-item-availability',
    ]))->postJson("/api/pos/v1/delivery/items/{$item->id}/availability", [
        'sold_out' => true,
    ])->assertOk()
        ->assertJsonPath('data.sold_out', true);

    expect(CatalogItem::query()->findOrFail($item->id)->sold_out)->toBeTrue();
    $job = new PushDeliveryUpdateJob('cfg-id', 'set_item_availability', ['catalog_item_id' => $item->id]);
    expect($job->operation)->toBe('set_item_availability');
});

it('propagates store availability from POS endpoint and persists channel metadata', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    $config = DeliveryChannelConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'channel_key' => 'aggregator',
        'is_enabled' => true,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-delivery-store-availability',
    ]))->postJson('/api/pos/v1/delivery/store-availability', [
        'state' => 'pause',
        'reason' => 'short maintenance',
    ])->assertOk()
        ->assertJsonPath('data.channels.0', 'aggregator');

    $metadata = DeliveryChannelConfig::query()->findOrFail($config->id)->metadata;
    expect((string) ($metadata['store_availability']['state'] ?? ''))->toBe('pause');
});
