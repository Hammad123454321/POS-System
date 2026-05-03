<?php

namespace App\Modules\DeliveryIntegrations\Application;

use App\Modules\DeliveryIntegrations\Application\Adapters\AggregatorAdapter;
use App\Modules\DeliveryIntegrations\Application\Adapters\DoorDashAdapter;
use App\Modules\DeliveryIntegrations\Application\Adapters\UberEatsAdapter;
use App\Modules\DeliveryIntegrations\Contracts\OnlineOrderingChannelAdapter;
use DomainException;

class DeliveryAdapterRegistry
{
    public function forChannel(string $channelKey): OnlineOrderingChannelAdapter
    {
        return match ($channelKey) {
            'aggregator' => app(AggregatorAdapter::class),
            'uber_eats' => app(UberEatsAdapter::class),
            'door_dash' => app(DoorDashAdapter::class),
            default => throw new DomainException("Unsupported delivery channel [{$channelKey}]."),
        };
    }
}
