<?php

namespace App\Modules\Billing\Application\Listeners;

use App\Modules\Billing\Application\RecordUsage;
use App\Modules\OrderRegister\Domain\Events\OrderPaid;
use Illuminate\Contracts\Queue\ShouldQueue;

class RecordOrderPaidUsage implements ShouldQueue
{
    public string $queue = 'reporting';

    public function __construct(
        private readonly RecordUsage $recordUsage,
    ) {}

    public function handle(OrderPaid $event): void
    {
        $this->recordUsage->handle(
            merchantId: $event->merchantId,
            storeId: $event->storeId,
            metricKey: 'orders.paid',
            quantity: 1,
            metadata: ['order_id' => $event->orderId, 'total_minor' => $event->totalMinor],
            sourceRef: $event->orderId,
        );
    }
}
