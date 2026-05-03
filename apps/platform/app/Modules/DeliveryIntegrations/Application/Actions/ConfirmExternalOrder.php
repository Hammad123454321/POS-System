<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\DeliveryIntegrations\Application\Jobs\PushDeliveryUpdateJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;
use DomainException;

class ConfirmExternalOrder
{
    public function handle(Device $device, ExternalOrderLink $externalOrderLink): ExternalOrderLink
    {
        if ($externalOrderLink->merchant_id !== $device->merchant_id || $externalOrderLink->store_id !== $device->store_id) {
            throw new DomainException('The requested delivery order does not belong to this device context.');
        }

        /** @var DeliveryChannelConfig|null $config */
        $config = DeliveryChannelConfig::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('channel_key', $externalOrderLink->channel_key)
            ->where('is_enabled', true)
            ->first();

        if ($config === null) {
            throw new DomainException('Delivery channel is not configured.');
        }

        PushDeliveryUpdateJob::dispatch(
            $config->id,
            'confirm_order',
            [
                'external_order_id' => $externalOrderLink->external_order_id,
                'internal_order_id' => $externalOrderLink->order_id,
            ],
        )->onConnection((string) config('pos.delivery.queue_connection', config('queue.default')))
            ->onQueue((string) config('pos.delivery.queue', 'delivery'));

        $externalOrderLink->forceFill([
            'status' => 'accepted',
            'processed_at' => CarbonImmutable::now('UTC'),
        ])->save();

        return $externalOrderLink->refresh();
    }
}
