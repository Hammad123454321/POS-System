<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\PlatformCore\Domain\Models\Store;
use DomainException;

class UpsertDeliveryChannelConfig
{
    /**
     * @param  array<string, mixed>|null  $credentials
     * @param  array<string, mixed>|null  $mapping
     * @param  array<int, array<string, mixed>>|null  $pauseWindows
     */
    public function handle(
        Store $store,
        string $channelKey,
        bool $isEnabled,
        ?array $credentials = null,
        ?array $mapping = null,
        ?array $pauseWindows = null,
        ?int $defaultPrepTimeMinutes = null,
        ?bool $syncHoursEnabled = null,
        ?bool $syncPrepTimeEnabled = null,
        ?bool $syncMenuEnabled = null,
    ): DeliveryChannelConfig {
        if (! in_array($channelKey, ['aggregator', 'uber_eats', 'door_dash'], true)) {
            throw new DomainException('Unsupported delivery channel key.');
        }

        return DeliveryChannelConfig::query()->updateOrCreate(
            [
                'merchant_id' => $store->merchant_id,
                'store_id' => $store->id,
                'channel_key' => $channelKey,
            ],
            [
                'is_enabled' => $isEnabled,
                'credentials' => $credentials,
                'mapping' => $mapping,
                'pause_windows' => $pauseWindows,
                'default_prep_time_minutes' => $defaultPrepTimeMinutes ?? 20,
                'sync_hours_enabled' => $syncHoursEnabled ?? true,
                'sync_prep_time_enabled' => $syncPrepTimeEnabled ?? true,
                'sync_menu_enabled' => $syncMenuEnabled ?? true,
            ],
        );
    }
}
