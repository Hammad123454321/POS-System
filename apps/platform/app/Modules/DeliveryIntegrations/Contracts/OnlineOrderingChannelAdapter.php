<?php

namespace App\Modules\DeliveryIntegrations\Contracts;

interface OnlineOrderingChannelAdapter
{
    /**
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, mixed>  $canonicalMenu
     * @return array<string, mixed>
     */
    public function publishMenu(array $channelConfig, array $canonicalMenu): array;

    /**
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, mixed>  $availability
     * @return array<string, mixed>
     */
    public function setStoreAvailability(array $channelConfig, array $availability): array;

    /**
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, mixed>  $availability
     * @return array<string, mixed>
     */
    public function setItemAvailability(array $channelConfig, array $availability): array;

    /**
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, mixed>  $payload
     * @return array<string, mixed>
     */
    public function receiveOrder(array $channelConfig, array $payload): array;

    /**
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, mixed>  $payload
     * @return array<string, mixed>
     */
    public function confirmOrder(array $channelConfig, array $payload): array;

    /**
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, mixed>  $payload
     * @return array<string, mixed>
     */
    public function updateStatus(array $channelConfig, array $payload): array;

    /**
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, mixed>  $payload
     * @return array<string, mixed>
     */
    public function cancelOrder(array $channelConfig, array $payload): array;

    /**
     * Verify the HMAC signature on an inbound provider webhook.
     *
     * @param  array<string, mixed>  $channelConfig
     * @param  array<string, string>  $headers  Lower-cased header name => value
     */
    public function verifyWebhookSignature(array $channelConfig, string $rawBody, array $headers): bool;

    /**
     * The lower-cased HTTP header carrying this channel's webhook signature.
     */
    public function webhookSignatureHeader(): string;
}
