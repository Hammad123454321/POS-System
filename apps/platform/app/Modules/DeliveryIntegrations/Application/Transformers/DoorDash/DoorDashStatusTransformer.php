<?php

namespace App\Modules\DeliveryIntegrations\Application\Transformers\DoorDash;

use App\Modules\DeliveryIntegrations\Contracts\DeliveryStatusTransformer;
use DomainException;

class DoorDashStatusTransformer implements DeliveryStatusTransformer
{
    public function toProviderStatus(string $canonicalStatus): string
    {
        return match ($canonicalStatus) {
            'accepted' => 'confirmed',
            'preparing' => 'preparing',
            'ready' => 'ready_for_pickup',
            'completed' => 'delivered',
            'cancelled' => 'cancelled',
            default => throw new DomainException('Unsupported canonical status for DoorDash.'),
        };
    }
}
