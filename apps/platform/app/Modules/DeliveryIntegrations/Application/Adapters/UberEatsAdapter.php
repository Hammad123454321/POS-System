<?php

namespace App\Modules\DeliveryIntegrations\Application\Adapters;

use App\Modules\DeliveryIntegrations\Application\Transformers\UberEats\UberEatsMenuTransformer;
use App\Modules\DeliveryIntegrations\Application\Transformers\UberEats\UberEatsOrderTransformer;
use App\Modules\DeliveryIntegrations\Application\Transformers\UberEats\UberEatsStatusTransformer;
use App\Modules\DeliveryIntegrations\Infrastructure\Http\DeliveryHttpClient;
use Carbon\CarbonImmutable;

class UberEatsAdapter extends AggregatorAdapter
{
    public function __construct(
        private readonly UberEatsMenuTransformer $menuTransformer,
        private readonly UberEatsOrderTransformer $orderTransformer,
        private readonly UberEatsStatusTransformer $statusTransformer,
        private readonly DeliveryHttpClient $httpClient,
    ) {}

    public function publishMenu(array $channelConfig, array $canonicalMenu): array
    {
        $providerMenu = $this->menuTransformer->toProviderMenu($canonicalMenu);

        $transport = $this->httpClient->send('uber_eats', $channelConfig, 'PUT', '/v1/eats/stores/menu', $providerMenu);

        return [
            'channel_key' => 'uber_eats',
            'status' => 'published',
            'published_at' => CarbonImmutable::now('UTC')->toIso8601String(),
            'item_count' => count($providerMenu['menus'][0]['items'] ?? []),
            'payload' => $providerMenu,
            'transport' => $transport['transport'] ?? 'stub',
        ];
    }

    public function receiveOrder(array $channelConfig, array $payload): array
    {
        $canonical = $this->orderTransformer->toCanonicalOrder($payload);

        return [
            'channel_key' => 'uber_eats',
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
            'channel_key' => 'uber_eats',
            'status' => 'updated',
            'external_order_id' => $payload['external_order_id'] ?? null,
            'order_status' => $providerStatus,
        ];
    }

    public function webhookSignatureHeader(): string
    {
        return 'x-uber-signature';
    }
}
