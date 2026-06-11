<?php

namespace App\Modules\Catalog\Application\Items;

use App\Modules\Catalog\Domain\Models\CatalogItem;

class UpdateCatalogItem
{
    /**
     * @param  array<string, mixed>  $attributes
     */
    public function handle(CatalogItem $item, array $attributes): CatalogItem
    {
        $item->fill(array_filter([
            'category_id' => $attributes['category_id'] ?? null,
            'tax_rule_id' => $attributes['tax_rule_id'] ?? null,
            'name' => $attributes['name'] ?? null,
            'sku' => $attributes['sku'] ?? null,
            'description' => $attributes['description'] ?? null,
            'base_price_minor' => isset($attributes['base_price_minor']) ? (int) $attributes['base_price_minor'] : null,
            'currency' => isset($attributes['currency']) ? strtoupper($attributes['currency']) : null,
        ], static fn ($value) => $value !== null));

        if (array_key_exists('sold_out', $attributes)) {
            $item->sold_out = (bool) $attributes['sold_out'];
        }

        $item->save();

        return $item->refresh();
    }
}
