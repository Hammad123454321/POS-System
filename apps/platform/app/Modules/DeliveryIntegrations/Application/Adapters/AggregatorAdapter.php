<?php

namespace App\Modules\DeliveryIntegrations\Application\Adapters;

use App\Modules\DeliveryIntegrations\Contracts\OnlineOrderingChannelAdapter;
use Carbon\CarbonImmutable;

class AggregatorAdapter implements OnlineOrderingChannelAdapter
{
    public function publishMenu(array $channelConfig, array $canonicalMenu): array
    {
        return [
            'channel_key' => 'aggregator',
            'status' => 'published',
            'published_at' => CarbonImmutable::now('UTC')->toIso8601String(),
            'item_count' => count($canonicalMenu['items'] ?? []),
        ];
    }

    public function setStoreAvailability(array $channelConfig, array $availability): array
    {
        return [
            'channel_key' => 'aggregator',
            'status' => 'updated',
            'availability' => $availability,
        ];
    }

    public function setItemAvailability(array $channelConfig, array $availability): array
    {
        return [
            'channel_key' => 'aggregator',
            'status' => 'updated',
            'availability' => $availability,
        ];
    }

    public function receiveOrder(array $channelConfig, array $payload): array
    {
        return [
            'channel_key' => 'aggregator',
            'status' => 'received',
            'external_order_id' => $payload['external_order_id'] ?? null,
            'payload' => $payload,
        ];
    }

    public function confirmOrder(array $channelConfig, array $payload): array
    {
        return [
            'channel_key' => 'aggregator',
            'status' => 'accepted',
            'external_order_id' => $payload['external_order_id'] ?? null,
        ];
    }

    public function updateStatus(array $channelConfig, array $payload): array
    {
        return [
            'channel_key' => 'aggregator',
            'status' => 'updated',
            'external_order_id' => $payload['external_order_id'] ?? null,
            'order_status' => $payload['status'] ?? null,
        ];
    }

    public function cancelOrder(array $channelConfig, array $payload): array
    {
        return [
            'channel_key' => 'aggregator',
            'status' => 'cancelled',
            'external_order_id' => $payload['external_order_id'] ?? null,
            'reason' => $payload['reason'] ?? null,
        ];
    }

    public function webhookSignatureHeader(): string
    {
        return 'x-webhook-signature';
    }

    public function verifyWebhookSignature(array $channelConfig, string $rawBody, array $headers): bool
    {
        $secret = (string) ($channelConfig['credentials']['webhook_secret'] ?? '');

        if ($secret === '') {
            return false;
        }

        $provided = (string) ($headers[$this->webhookSignatureHeader()] ?? '');

        if ($provided === '') {
            return false;
        }

        $expected = hash_hmac('sha256', $rawBody, $secret);

        return hash_equals($expected, strtolower($provided));
    }
}
