<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\DeliveryIntegrations\Application\Jobs\PushDeliveryUpdateJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;

class PropagateStoreAvailability
{
    public function __construct(
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    /**
     * @return array<int, string>
     */
    public function handle(
        Device $device,
        string $state,
        ?string $reason = null,
        ?string $channelKey = null,
    ): array {
        $resolvedState = $state === 'resume' ? 'open' : $state;

        $configs = DeliveryChannelConfig::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('is_enabled', true)
            ->when($channelKey !== null, fn ($query) => $query->where('channel_key', $channelKey))
            ->get();

        if ($configs->isEmpty()) {
            $this->openExceptionCase->handle(
                merchantId: $device->merchant_id,
                storeId: $device->store_id,
                module: 'delivery',
                code: 'missing_delivery_channel_config',
                severity: 'low',
                title: 'Store availability was changed but no enabled delivery channel exists.',
                details: [
                    'state' => $resolvedState,
                    'reason' => $reason,
                    'channel_key' => $channelKey,
                ],
                relatedType: 'delivery_channel_config',
                relatedId: null,
                openedByDeviceId: $device->id,
            );

            return [];
        }

        $propagatedChannels = [];
        $propagatedAt = CarbonImmutable::now('UTC')->toIso8601String();

        foreach ($configs as $config) {
            PushDeliveryUpdateJob::dispatch(
                $config->id,
                'set_store_availability',
                [
                    'state' => $resolvedState,
                    'reason' => $reason,
                    'store_id' => $device->store_id,
                ],
            )->onConnection((string) config('pos.delivery.queue_connection', config('queue.default')))
                ->onQueue((string) config('pos.delivery.queue', 'delivery'));

            $metadata = $config->metadata ?? [];
            $metadata['store_availability'] = [
                'state' => $resolvedState,
                'reason' => $reason,
                'propagated_at' => $propagatedAt,
                'updated_by_device_id' => $device->id,
            ];
            $config->forceFill(['metadata' => $metadata])->save();
            $propagatedChannels[] = (string) $config->channel_key;
        }

        return $propagatedChannels;
    }
}
