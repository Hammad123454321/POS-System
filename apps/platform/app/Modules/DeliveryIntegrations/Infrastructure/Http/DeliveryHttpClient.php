<?php

namespace App\Modules\DeliveryIntegrations\Infrastructure\Http;

use App\Modules\DeliveryIntegrations\Application\Exceptions\DeliveryTransportException;
use Illuminate\Http\Client\PendingRequest;
use Illuminate\Support\Facades\Http;

/**
 * Outbound HTTP transport for delivery channel adapters. When the channel has no
 * configured base URL (or sandbox mode is on with no sandbox URL) it short-circuits
 * to a canned stub response flagged `transport=stub`, preserving the previous
 * no-network behavior and keeping tests hermetic.
 */
class DeliveryHttpClient
{
    /**
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, mixed>  $body
     * @return array<string, mixed>
     */
    public function send(string $channelKey, array $channelConfig, string $method, string $path, array $body): array
    {
        $baseUrl = $this->resolveBaseUrl($channelKey);

        if ($baseUrl === null) {
            return ['transport' => 'stub', 'channel_key' => $channelKey, 'path' => $path];
        }

        $response = $this->pending($baseUrl, $channelConfig)->send($method, $path, ['json' => $body]);

        if ($response->failed()) {
            throw new DeliveryTransportException(
                "Delivery channel [{$channelKey}] returned HTTP {$response->status()} for {$path}.",
            );
        }

        return ['transport' => 'http', 'status' => $response->status(), 'body' => $response->json()];
    }

    /**
     * @param  array<string, mixed>  $channelConfig
     */
    private function pending(string $baseUrl, array $channelConfig): PendingRequest
    {
        $token = (string) ($channelConfig['credentials']['access_token'] ?? '');

        $request = Http::baseUrl($baseUrl)
            ->timeout((int) config('pos.delivery_channels.timeout_seconds', 10))
            ->connectTimeout((int) config('pos.delivery_channels.connect_timeout_seconds', 3))
            ->retry(
                (int) config('pos.delivery_channels.retries', 3),
                (int) config('pos.delivery_channels.retry_backoff_ms', 250),
                throw: false,
            )
            ->acceptJson();

        if ($token !== '') {
            $request = $request->withToken($token);
        }

        return $request;
    }

    private function resolveBaseUrl(string $channelKey): ?string
    {
        $sandbox = (bool) config('pos.delivery_channels.sandbox', true);
        $channel = (array) config("pos.delivery_channels.{$channelKey}", []);

        if ($sandbox) {
            $sandboxUrl = $channel['sandbox_base_url'] ?? null;

            return is_string($sandboxUrl) && $sandboxUrl !== '' ? $sandboxUrl : null;
        }

        $baseUrl = $channel['base_url'] ?? null;

        return is_string($baseUrl) && $baseUrl !== '' ? $baseUrl : null;
    }
}
