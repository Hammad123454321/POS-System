<?php

namespace App\Modules\DeliveryIntegrations\Application\Adapters;

use App\Modules\DeliveryIntegrations\Application\Transformers\DoorDash\DoorDashMenuTransformer;
use App\Modules\DeliveryIntegrations\Application\Transformers\DoorDash\DoorDashOrderTransformer;
use App\Modules\DeliveryIntegrations\Application\Transformers\DoorDash\DoorDashStatusTransformer;
use Carbon\CarbonImmutable;

class DoorDashAdapter extends AggregatorAdapter
{
    public function __construct(
        private readonly DoorDashMenuTransformer $menuTransformer,
        private readonly DoorDashOrderTransformer $orderTransformer,
        private readonly DoorDashStatusTransformer $statusTransformer,
    ) {}

    public function publishMenu(array $channelConfig, array $canonicalMenu): array
    {
        $providerMenu = $this->menuTransformer->toProviderMenu($canonicalMenu);

        return [
            'channel_key' => 'door_dash',
            'status' => 'published',
            'published_at' => CarbonImmutable::now('UTC')->toIso8601String(),
            'item_count' => count($providerMenu['menu_items'] ?? []),
            'payload' => $providerMenu,
        ];
    }

    public function receiveOrder(array $channelConfig, array $payload): array
    {
        $canonical = $this->orderTransformer->toCanonicalOrder($payload);

        return [
            'channel_key' => 'door_dash',
            'status' => 'received',
            'external_order_id' => $canonical['external_order_id'] ?? null,
            'payload' => $canonical,
        ];
    }

    public function updateStatus(array $channelConfig, array $payload): array
    {
        $canonicalStatus = (string) ($payload['status'] ?? '');
        $providerStatus = $this->statusTransformer->toProviderStatus($canonicalStatus);

        return [
            'channel_key' => 'door_dash',
            'status' => 'updated',
            'external_order_id' => $payload['external_order_id'] ?? null,
            'order_status' => $providerStatus,
        ];
    }
}
