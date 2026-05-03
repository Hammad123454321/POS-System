<?php

use App\Modules\DeliveryIntegrations\Application\DeliveryAdapterRegistry;

it('enforces shared online ordering adapter contract across all phase 4 delivery channels', function () {
    $registry = app(DeliveryAdapterRegistry::class);

    foreach (['aggregator', 'uber_eats', 'door_dash'] as $channelKey) {
        $adapter = $registry->forChannel($channelKey);
        $config = ['channel_key' => $channelKey, 'credentials' => ['token' => 'test']];
        $payload = [
            'external_order_id' => 'ext-order-1',
            'status' => 'ready',
            'line_items' => [
                ['sku' => 'ITEM-1', 'quantity' => 1],
            ],
        ];

        $publish = $adapter->publishMenu($config, [
            'store_id' => 'store-1',
            'items' => [['id' => 'item-1', 'name' => 'Item 1', 'base_price_minor' => 1200, 'currency' => 'USD', 'sold_out' => false]],
        ]);
        $storeAvailability = $adapter->setStoreAvailability($config, ['is_open' => true]);
        $itemAvailability = $adapter->setItemAvailability($config, ['sku' => 'ITEM-1', 'sold_out' => false]);
        $received = $adapter->receiveOrder($config, $payload);
        $confirmed = $adapter->confirmOrder($config, $payload);
        $updated = $adapter->updateStatus($config, $payload);
        $cancelled = $adapter->cancelOrder($config, $payload);

        expect($publish)->toBeArray();
        expect($storeAvailability)->toBeArray();
        expect($itemAvailability)->toBeArray();
        expect($received)->toBeArray();
        expect($confirmed)->toBeArray();
        expect($updated)->toBeArray();
        expect($cancelled)->toBeArray();
        expect($received['external_order_id'] ?? null)->toBe('ext-order-1');
        expect($publish['channel_key'] ?? null)->toBe($channelKey);
        expect($received['channel_key'] ?? null)->toBe($channelKey);
    }
});
