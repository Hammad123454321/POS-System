<?php

namespace App\Modules\DeliveryIntegrations\Application\Transformers\DoorDash;

use App\Modules\DeliveryIntegrations\Contracts\DeliveryMenuTransformer;

class DoorDashMenuTransformer implements DeliveryMenuTransformer
{
    public function toProviderMenu(array $canonicalMenu): array
    {
        return [
            'store' => [
                'id' => $canonicalMenu['store_id'] ?? null,
            ],
            'menu_items' => collect($canonicalMenu['items'] ?? [])->map(fn (array $item): array => [
                'merchant_supplied_id' => $item['id'] ?? null,
                'name' => $item['name'] ?? null,
                'price' => $item['base_price_minor'] ?? null,
                'currency' => $item['currency'] ?? null,
                'active' => ! (bool) ($item['sold_out'] ?? false),
                'sku' => $item['sku'] ?? null,
            ])->values()->all(),
        ];
    }
}
