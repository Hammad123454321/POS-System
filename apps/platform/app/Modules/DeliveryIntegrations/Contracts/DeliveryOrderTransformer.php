<?php

namespace App\Modules\DeliveryIntegrations\Contracts;

interface DeliveryOrderTransformer
{
    /**
     * @param  array<string, mixed>  $providerPayload
     * @return array<string, mixed>
     */
    public function toCanonicalOrder(array $providerPayload): array;
}
