<?php

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryProviderEvent;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;

function deliveryWebhookContext(string $secret = 'whsec-123', string $externalStoreId = 'EXT-1'): array
{
    $merchant = Merchant::query()->create(['name' => 'Deli', 'currency' => 'USD', 'status' => 'active']);
    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Store',
        'code' => 'DW',
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    $config = DeliveryChannelConfig::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'channel_key' => 'uber_eats',
        'external_store_id' => $externalStoreId,
        'is_enabled' => true,
        'credentials' => ['webhook_secret' => $secret],
        'mapping' => ['external_store_id' => $externalStoreId],
    ]);

    $item = CatalogItem::query()->create([
        'merchant_id' => $merchant->id,
        'type' => 'product',
        'name' => 'Burger',
        'sku' => 'BRG',
        'base_price_minor' => 1200,
        'currency' => 'USD',
        'is_active' => true,
        'sold_out' => false,
    ]);

    return [$merchant, $store, $config, $item];
}

function signedDelivery(string $secret, array $payload): array
{
    $raw = json_encode($payload);

    return [$raw, hash_hmac('sha256', $raw, $secret)];
}

it('accepts a valid signed order webhook and creates a link + order', function () {
    [$merchant, $store, $config, $item] = deliveryWebhookContext();

    $payload = [
        'event_id' => 'evt-1',
        'event_type' => 'order_create',
        'external_store_id' => 'EXT-1',
        'external_order_id' => 'UB-1001',
        'line_items' => [['catalog_item_id' => $item->id, 'quantity' => 2]],
    ];
    [$raw, $sig] = signedDelivery('whsec-123', $payload);

    $this->call('POST', '/api/webhooks/delivery/uber_eats', [], [], [], [
        'HTTP_X_UBER_SIGNATURE' => $sig,
        'CONTENT_TYPE' => 'application/json',
    ], $raw)->assertOk()->assertJsonPath('status', 'accepted');

    expect(DeliveryProviderEvent::query()->where('external_event_id', 'evt-1')->where('signature_valid', true)->exists())->toBeTrue();
    expect(ExternalOrderLink::query()->where('external_order_id', 'UB-1001')->exists())->toBeTrue();
});

it('is idempotent for a replayed event id', function () {
    [$merchant, $store, $config, $item] = deliveryWebhookContext();

    $payload = [
        'event_id' => 'evt-dup',
        'event_type' => 'order_create',
        'external_store_id' => 'EXT-1',
        'external_order_id' => 'UB-2',
        'line_items' => [['catalog_item_id' => $item->id, 'quantity' => 1]],
    ];
    [$raw, $sig] = signedDelivery('whsec-123', $payload);

    foreach (range(1, 2) as $i) {
        $this->call('POST', '/api/webhooks/delivery/uber_eats', [], [], [], [
            'HTTP_X_UBER_SIGNATURE' => $sig,
            'CONTENT_TYPE' => 'application/json',
        ], $raw)->assertOk();
    }

    expect(DeliveryProviderEvent::query()->where('external_event_id', 'evt-dup')->count())->toBe(1);
    expect(ExternalOrderLink::query()->where('external_order_id', 'UB-2')->count())->toBe(1);
});

it('rejects a bad signature with 401 and opens an exception case', function () {
    [$merchant, $store, $config, $item] = deliveryWebhookContext();

    $payload = [
        'event_id' => 'evt-bad',
        'event_type' => 'order_create',
        'external_store_id' => 'EXT-1',
        'external_order_id' => 'UB-3',
    ];
    $raw = json_encode($payload);

    $this->call('POST', '/api/webhooks/delivery/uber_eats', [], [], [], [
        'HTTP_X_UBER_SIGNATURE' => 'deadbeef',
        'CONTENT_TYPE' => 'application/json',
    ], $raw)->assertStatus(401);

    expect(DeliveryProviderEvent::query()->where('external_event_id', 'evt-bad')->where('signature_valid', false)->exists())->toBeTrue();
    expect(ExceptionCase::query()->where('code', 'webhook_signature_invalid')->exists())->toBeTrue();
});

it('opens an exception for an unknown external store', function () {
    deliveryWebhookContext();

    $payload = [
        'event_id' => 'evt-unknown',
        'event_type' => 'order_create',
        'external_store_id' => 'NOPE',
        'external_order_id' => 'UB-4',
    ];
    $raw = json_encode($payload);
    // No config matches NOPE → signature can't be verified → 401.
    $this->call('POST', '/api/webhooks/delivery/uber_eats', [], [], [], [
        'HTTP_X_UBER_SIGNATURE' => 'whatever',
        'CONTENT_TYPE' => 'application/json',
    ], $raw)->assertStatus(401);

    expect(ExceptionCase::query()->where('code', 'webhook_signature_invalid')->exists())->toBeTrue();
});
