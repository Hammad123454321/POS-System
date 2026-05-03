<?php

namespace App\Modules\DeliveryIntegrations\Application\Queries;

use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\PlatformCore\Domain\Models\Device;

class DeliveryChannelConfigSnapshotQuery
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function forDevice(Device $device): array
    {
        return DeliveryChannelConfig::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('is_enabled', true)
            ->orderBy('channel_key')
            ->get()
            ->map(fn (DeliveryChannelConfig $config): array => [
                'id' => $config->id,
                'channel_key' => $config->channel_key,
                'is_enabled' => $config->is_enabled,
                'mapping' => $config->mapping,
                'pause_windows' => $config->pause_windows,
                'default_prep_time_minutes' => $config->default_prep_time_minutes,
                'sync_hours_enabled' => $config->sync_hours_enabled,
                'sync_prep_time_enabled' => $config->sync_prep_time_enabled,
                'sync_menu_enabled' => $config->sync_menu_enabled,
            ])
            ->values()
            ->all();
    }
}
