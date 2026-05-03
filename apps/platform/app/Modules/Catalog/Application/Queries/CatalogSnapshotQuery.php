<?php

namespace App\Modules\Catalog\Application\Queries;

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\Category;
use App\Modules\Catalog\Domain\Models\ComboPackage;
use App\Modules\Catalog\Domain\Models\ModifierGroup;
use App\Modules\Catalog\Domain\Models\PriceRule;
use App\Modules\Catalog\Domain\Models\TaxRule;
use App\Modules\Catalog\Domain\Models\Variant;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;

class CatalogSnapshotQuery
{
    /**
     * @return array<string, mixed>
     */
    public function forDevice(Device $device): array
    {
        $now = CarbonImmutable::now('UTC');

        $categories = Category::query()
            ->forMerchant($device->merchant_id)
            ->where('is_active', true)
            ->where(function ($query) use ($device): void {
                $query->whereNull('store_id')
                    ->orWhere('store_id', $device->store_id);
            })
            ->orderBy('sort_order')
            ->get(['id', 'name', 'sort_order', 'store_id']);

        $items = CatalogItem::query()
            ->forMerchant($device->merchant_id)
            ->where('is_active', true)
            ->with(['priceRules' => function ($query) use ($now): void {
                $query->where('is_active', true)
                    ->where(function ($inner) use ($now): void {
                        $inner->whereNull('starts_at')->orWhere('starts_at', '<=', $now);
                    })
                    ->where(function ($inner) use ($now): void {
                        $inner->whereNull('ends_at')->orWhere('ends_at', '>=', $now);
                    })
                    ->orderByDesc('priority');
            }])
            ->with([
                'variants' => fn ($query) => $query->where('is_active', true)->orderBy('sort_order'),
                'modifierGroups' => fn ($query) => $query->where('is_active', true)->orderBy('sort_order')->with([
                    'options' => fn ($optQuery) => $optQuery->where('is_active', true)->orderBy('sort_order'),
                ]),
                'addOnMappings.addOnItem',
            ])
            ->get(['id', 'category_id', 'name', 'sku', 'type', 'base_price_minor', 'currency', 'tax_rule_id', 'sold_out']);

        $combos = ComboPackage::query()
            ->forMerchant($device->merchant_id)
            ->where('is_active', true)
            ->where(function ($query) use ($device): void {
                $query->whereNull('store_id')
                    ->orWhere('store_id', $device->store_id);
            })
            ->with(['items.catalogItem', 'addOns.catalogItem'])
            ->orderBy('sort_order')
            ->get();

        $taxRules = TaxRule::query()
            ->forMerchant($device->merchant_id)
            ->where('is_active', true)
            ->get(['id', 'name', 'code', 'rate_basis_points', 'is_inclusive']);

        return [
            'categories' => $categories->map(fn (Category $category): array => [
                'id' => $category->id,
                'name' => $category->name,
                'sort_order' => $category->sort_order,
                'store_id' => $category->store_id,
            ])->values()->all(),
            'tax_rules' => $taxRules->map(fn (TaxRule $rule): array => [
                'id' => $rule->id,
                'name' => $rule->name,
                'code' => $rule->code,
                'rate_basis_points' => $rule->rate_basis_points,
                'is_inclusive' => $rule->is_inclusive,
            ])->values()->all(),
            'items' => $items->map(function (CatalogItem $item): array {
                /** @var PriceRule|null $activePrice */
                $activePrice = $item->priceRules->first();

                return [
                    'id' => $item->id,
                    'category_id' => $item->category_id,
                    'name' => $item->name,
                    'sku' => $item->sku,
                    'type' => $item->type,
                    'currency' => $item->currency,
                    'base_price_minor' => $item->base_price_minor,
                    'effective_price_minor' => $activePrice?->price_minor ?? $item->base_price_minor,
                    'tax_rule_id' => $item->tax_rule_id,
                    'sold_out' => $item->sold_out,
                    'variants' => $item->variants->map(fn (Variant $variant): array => [
                        'id' => $variant->id,
                        'name' => $variant->name,
                        'code' => $variant->code,
                        'options' => $variant->options ?? [],
                        'price_delta_minor' => (int) $variant->price_delta_minor,
                    ])->values()->all(),
                    'modifier_groups' => $item->modifierGroups->map(fn (ModifierGroup $group): array => [
                        'id' => $group->id,
                        'name' => $group->name,
                        'selection_mode' => $group->selection_mode,
                        'min_select' => (int) $group->min_select,
                        'max_select' => $group->max_select,
                        'is_required' => (bool) $group->is_required,
                        'options' => $group->options->map(fn ($option): array => [
                            'id' => $option->id,
                            'name' => $option->name,
                            'code' => $option->code,
                            'price_delta_minor' => (int) $option->price_delta_minor,
                        ])->values()->all(),
                    ])->values()->all(),
                    'add_ons' => $item->addOnMappings
                        ->filter(fn ($mapping) => $mapping->addOnItem !== null)
                        ->map(fn ($mapping): array => [
                            'catalog_item_id' => $mapping->addOnItem->id,
                            'name' => $mapping->addOnItem->name,
                            'sku' => $mapping->addOnItem->sku,
                            'base_price_minor' => (int) $mapping->addOnItem->base_price_minor,
                        ])->values()->all(),
                ];
            })->values()->all(),
            'combo_packages' => $combos->map(fn (ComboPackage $combo): array => [
                'id' => $combo->id,
                'name' => $combo->name,
                'code' => $combo->code,
                'price_minor' => (int) $combo->price_minor,
                'currency' => $combo->currency,
                'items' => $combo->items->filter(fn ($item) => $item->catalogItem !== null)->map(fn ($item): array => [
                    'catalog_item_id' => $item->catalogItem->id,
                    'name' => $item->catalogItem->name,
                    'quantity' => (int) $item->quantity,
                ])->values()->all(),
                'add_ons' => $combo->addOns->filter(fn ($addOn) => $addOn->catalogItem !== null)->map(fn ($addOn): array => [
                    'catalog_item_id' => $addOn->catalogItem->id,
                    'name' => $addOn->catalogItem->name,
                ])->values()->all(),
            ])->values()->all(),
        ];
    }
}
