<?php

namespace App\Modules\DeliveryIntegrations\Application\Queries;

use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\PlatformCore\Domain\Models\Device;

class ListPendingExternalOrdersForDevice
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function handle(Device $device): array
    {
        return ExternalOrderLink::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->whereNull('processed_at')
            ->orderBy('received_at')
            ->limit(100)
            ->get()
            ->map(fn (ExternalOrderLink $link): array => [
                'id' => $link->id,
                'channel_key' => $link->channel_key,
                'external_order_id' => $link->external_order_id,
                'status' => $link->status,
                'payload' => $link->payload,
                'received_at' => $link->received_at?->toIso8601String(),
            ])->values()
            ->all();
    }
}
