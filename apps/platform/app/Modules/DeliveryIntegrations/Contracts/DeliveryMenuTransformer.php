<?php

namespace App\Modules\DeliveryIntegrations\Contracts;

interface DeliveryMenuTransformer
{
    /**
     * @param  array<string, mixed>  $canonicalMenu
     * @return array<string, mixed>
     */
    public function toProviderMenu(array $canonicalMenu): array;
}
