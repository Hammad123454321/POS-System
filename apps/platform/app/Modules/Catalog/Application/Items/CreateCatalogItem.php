<?php

namespace App\Modules\Catalog\Application\Items;

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\PlatformCore\Domain\Models\Store;

class CreateCatalogItem
{
    /**
     * @param  array<string, mixed>  $attributes
     */
    public function handle(Store $store, array $attributes): CatalogItem
    {
        return CatalogItem::query()->create([
            'merchant_id' => $store->merchant_id,
            'category_id' => $attributes['category_id'] ?? null,
            'tax_rule_id' => $attributes['tax_rule_id'] ?? null,
            'type' => $attributes['type'],
            'name' => $attributes['name'],
            'sku' => $attributes['sku'] ?? null,
            'description' => $attributes['description'] ?? null,
            'base_price_minor' => (int) $attributes['base_price_minor'],
            'currency' => strtoupper($attributes['currency']),
            'is_active' => $attributes['is_active'] ?? true,
            'sold_out' => $attributes['sold_out'] ?? false,
        ]);
    }
}
