<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\DeliveryIntegrations\Application\Jobs\PushDeliveryUpdateJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;

class UpdateExternalOrderStatus
{
    public function __construct(
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    public function handle(Device $device, ExternalOrderLink $externalOrderLink, string $status): ExternalOrderLink
    {
        if ($externalOrderLink->merchant_id !== $device->merchant_id || $externalOrderLink->store_id !== $device->store_id) {
            throw new DomainException('The requested delivery order does not belong to this device context.');
        }

        if (! in_array($status, ['accepted', 'preparing', 'ready', 'completed', 'cancelled'], true)) {
            throw new DomainException('Unsupported delivery status transition.');
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

        if ($this->isOutOfOrderTransition((string) $externalOrderLink->status, $status)) {
            $this->openExceptionCase->handle(
                merchantId: $device->merchant_id,
                storeId: $device->store_id,
                module: 'delivery',
                code: 'state_mismatch',
                severity: 'medium',
                title: 'Out-of-order delivery status event detected.',
                details: [
                    'external_order_link_id' => $externalOrderLink->id,
                    'current_status' => $externalOrderLink->status,
                    'incoming_status' => $status,
                ],
                relatedType: 'external_order_link',
                relatedId: $externalOrderLink->id,
                openedByDeviceId: $device->id,
            );

            return $externalOrderLink->refresh();
        }

        PushDeliveryUpdateJob::dispatch(
            $config->id,
            'update_status',
            [
                'external_order_id' => $externalOrderLink->external_order_id,
                'internal_order_id' => $externalOrderLink->order_id,
                'status' => $status,
            ],
        )->onConnection((string) config('pos.delivery.queue_connection', config('queue.default')))
            ->onQueue((string) config('pos.delivery.queue', 'delivery'));

        if ($externalOrderLink->order_id !== null) {
            /** @var Order|null $order */
            $order = Order::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->whereKey($externalOrderLink->order_id)
                ->first();

            if ($order !== null && $status === 'cancelled') {
                $order->forceFill([
                    'status' => 'voided',
                ])->save();
            }
        }

        $externalOrderLink->forceFill([
            'status' => $status,
        ])->save();

        return $externalOrderLink->refresh();
    }

    private function isOutOfOrderTransition(string $currentStatus, string $incomingStatus): bool
    {
        $rank = [
            'received' => 1,
            'accepted' => 2,
            'preparing' => 3,
            'ready' => 4,
            'completed' => 5,
            'cancelled' => 6,
        ];

        $current = $rank[$currentStatus] ?? 0;
        $incoming = $rank[$incomingStatus] ?? 0;

        if ($currentStatus === 'cancelled' || $currentStatus === 'completed') {
            return $incomingStatus !== $currentStatus;
        }

        return $incoming < $current;
    }
}
