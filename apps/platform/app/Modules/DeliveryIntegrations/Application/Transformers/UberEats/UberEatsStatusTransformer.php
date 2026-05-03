<?php

namespace App\Modules\DeliveryIntegrations\Application\Transformers\UberEats;

use App\Modules\DeliveryIntegrations\Contracts\DeliveryStatusTransformer;
use DomainException;

class UberEatsStatusTransformer implements DeliveryStatusTransformer
{
    public function toProviderStatus(string $canonicalStatus): string
    {
        return match ($canonicalStatus) {
            'accepted' => 'accepted',
            'preparing' => 'in_progress',
            'ready' => 'ready_for_pickup',
            'completed' => 'completed',
            'cancelled' => 'cancelled',
            default => throw new DomainException('Unsupported canonical status for Uber Eats.'),
        };
    }
}
