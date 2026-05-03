<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\DeliveryIntegrations\Application\Jobs\PublishDeliveryMenuJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\PlatformCore\Domain\Models\Store;

class DispatchMenuPublish
{
    public function handle(Store $store, DeliveryChannelConfig $deliveryChannelConfig): void
    {
        if ($deliveryChannelConfig->merchant_id !== $store->merchant_id || $deliveryChannelConfig->store_id !== $store->id) {
            return;
        }

        PublishDeliveryMenuJob::dispatch($deliveryChannelConfig->id)
            ->onConnection((string) config('pos.delivery.queue_connection', config('queue.default')))
            ->onQueue((string) config('pos.delivery.queue', 'delivery'));
    }
}
