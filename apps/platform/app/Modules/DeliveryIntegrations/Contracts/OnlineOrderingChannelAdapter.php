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
}
