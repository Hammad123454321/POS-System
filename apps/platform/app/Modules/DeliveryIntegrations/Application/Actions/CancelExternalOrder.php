<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\DeliveryIntegrations\Application\Jobs\PushDeliveryUpdateJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;

class CancelExternalOrder
{
    public function handle(Device $device, ExternalOrderLink $externalOrderLink, ?string $reason = null): ExternalOrderLink
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
            'cancel_order',
            [
                'external_order_id' => $externalOrderLink->external_order_id,
                'internal_order_id' => $externalOrderLink->order_id,
                'reason' => $reason,
            ],
        )->onConnection((string) config('pos.delivery.queue_connection', config('queue.default')))
            ->onQueue((string) config('pos.delivery.queue', 'delivery'));

        $externalOrderLink->forceFill([
            'status' => 'cancelled',
        ])->save();

        return $externalOrderLink->refresh();
    }
}
