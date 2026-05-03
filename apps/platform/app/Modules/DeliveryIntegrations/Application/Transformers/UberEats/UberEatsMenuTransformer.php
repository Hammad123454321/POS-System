<?php

namespace App\Modules\DeliveryIntegrations\Application\Transformers\UberEats;

use App\Modules\DeliveryIntegrations\Contracts\DeliveryMenuTransformer;

class UberEatsMenuTransformer implements DeliveryMenuTransformer
{
    public function toProviderMenu(array $canonicalMenu): array
    {
        return [
            'store_id' => $canonicalMenu['store_id'] ?? null,
            'menus' => [
                [
                    'title' => 'POS Menu',
                    'items' => collect($canonicalMenu['items'] ?? [])->map(fn (array $item): array => [
                        'external_id' => $item['id'] ?? null,
                        'name' => $item['name'] ?? null,
                        'price' => $item['base_price_minor'] ?? null,
                        'currency_code' => $item['currency'] ?? null,
                        'is_sold_out' => $item['sold_out'] ?? false,
                        'sku' => $item['sku'] ?? null,
                    ])->values()->all(),
                ],
            ],
        ];
    }
}
