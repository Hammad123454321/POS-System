<?php

namespace App\Modules\DeliveryIntegrations\Contracts;

interface DeliveryStatusTransformer
{
    public function toProviderStatus(string $canonicalStatus): string;
}
