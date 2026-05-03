<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\PlatformCore\Domain\Models\Store;

class BuildCanonicalMenuPayload
{
    /**
     * @return array<string, mixed>
     */
    public function handle(Store $store): array
    {
        $items = CatalogItem::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('is_active', true)
            ->with('taxRule')
            ->orderBy('name')
            ->get();

        return [
            'store_id' => $store->id,
            'merchant_id' => $store->merchant_id,
            'items' => $items->map(fn (CatalogItem $item): array => [
                'id' => $item->id,
                'name' => $item->name,
                'type' => $item->type,
                'sku' => $item->sku,
                'base_price_minor' => $item->base_price_minor,
                'currency' => $item->currency,
                'sold_out' => $item->sold_out,
                'tax' => $item->taxRule ? [
                    'code' => $item->taxRule->code,
                    'rate_basis_points' => $item->taxRule->rate_basis_points,
                    'is_inclusive' => $item->taxRule->is_inclusive,
                ] : null,
            ])->values()->all(),
        ];
    }
}
