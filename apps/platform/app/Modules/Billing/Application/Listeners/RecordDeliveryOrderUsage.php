<?php

namespace App\Modules\Billing\Application\Listeners;

use App\Modules\Billing\Application\RecordUsage;
use App\Modules\DeliveryIntegrations\Domain\Events\DeliveryOrderIngested;
use Illuminate\Contracts\Queue\ShouldQueue;

class RecordDeliveryOrderUsage implements ShouldQueue
{
    public string $queue = 'reporting';

    public function __construct(
        private readonly RecordUsage $recordUsage,
    ) {}

    public function handle(DeliveryOrderIngested $event): void
    {
        $this->recordUsage->handle(
            merchantId: $event->merchantId,
            storeId: $event->storeId,
            metricKey: 'delivery.orders.ingested',
            quantity: 1,
            metadata: ['external_order_link_id' => $event->externalOrderLinkId, 'channel_key' => $event->channelKey],
            sourceRef: $event->externalOrderLinkId,
        );
    }
}
