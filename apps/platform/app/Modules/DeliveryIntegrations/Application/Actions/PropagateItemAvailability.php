<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\DeliveryIntegrations\Application\Jobs\PushDeliveryUpdateJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;

class PropagateItemAvailability
{
    public function __construct(
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    public function handle(Device $device, CatalogItem $catalogItem, bool $soldOut): void
    {
        if ($catalogItem->merchant_id !== $device->merchant_id) {
            throw new DomainException('The requested catalog item does not belong to this device context.');
        }

        $catalogItem->forceFill([
            'sold_out' => $soldOut,
        ])->save();

        $configs = DeliveryChannelConfig::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('is_enabled', true)
            ->get();

        foreach ($configs as $config) {
            PushDeliveryUpdateJob::dispatch(
                $config->id,
                'set_item_availability',
                [
                    'catalog_item_id' => $catalogItem->id,
                    'sku' => $catalogItem->sku,
                    'sold_out' => $soldOut,
                ],
            )->onConnection((string) config('pos.delivery.queue_connection', config('queue.default')))
                ->onQueue((string) config('pos.delivery.queue', 'delivery'));
        }

        if ($configs->isEmpty()) {
            $this->openExceptionCase->handle(
                merchantId: $device->merchant_id,
                storeId: $device->store_id,
                module: 'delivery',
                code: 'missing_delivery_channel_config',
                severity: 'low',
                title: 'Sold-out state changed but no delivery channel config is enabled.',
                details: [
                    'catalog_item_id' => $catalogItem->id,
                    'sku' => $catalogItem->sku,
                    'sold_out' => $soldOut,
                ],
                relatedType: 'catalog_item',
                relatedId: $catalogItem->id,
                openedByDeviceId: $device->id,
            );
        }
    }
}
