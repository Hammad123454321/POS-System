<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\ComboPackage;
use App\Modules\Catalog\Domain\Models\ComboPackageAddOn;
use App\Modules\Catalog\Domain\Models\ComboPackageItem;
use App\Modules\Catalog\Interfaces\Http\Requests\UpsertComboPackageRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

class AdminComboPackageController extends Controller
{
    public function store(UpsertComboPackageRequest $request, Store $store): JsonResponse
    {
        $this->assertCatalogItemsInStoreMerchant(
            $store,
            collect($request->input('items', []))->pluck('catalog_item_id')->values()->all(),
        );
        $this->assertCatalogItemsInStoreMerchant(
            $store,
            collect($request->input('add_on_item_ids', []))->values()->all(),
        );

        $combo = DB::transaction(function () use ($request, $store): ComboPackage {
            $combo = ComboPackage::query()->create([
                'merchant_id' => $store->merchant_id,
                'store_id' => $store->id,
                'name' => $request->string('name')->toString(),
                'code' => $request->input('code'),
                'price_minor' => $request->integer('price_minor'),
                'currency' => $request->string('currency')->toString(),
                'sort_order' => (int) $request->input('sort_order', 0),
                'is_active' => (bool) $request->input('is_active', true),
            ]);

            $this->syncChildren($combo, $request->validated());

            return $combo;
        });

        return response()->json(['data' => ['id' => $combo->id]], 201);
    }

    public function update(UpsertComboPackageRequest $request, Store $store, ComboPackage $comboPackage): JsonResponse
    {
        abort_unless($comboPackage->merchant_id === $store->merchant_id && $comboPackage->store_id === $store->id, 404);
        $this->assertCatalogItemsInStoreMerchant(
            $store,
            collect($request->input('items', []))->pluck('catalog_item_id')->values()->all(),
        );
        $this->assertCatalogItemsInStoreMerchant(
            $store,
            collect($request->input('add_on_item_ids', []))->values()->all(),
        );

        DB::transaction(function () use ($comboPackage, $request): void {
            $comboPackage->forceFill([
                'name' => $request->string('name')->toString(),
                'code' => $request->input('code'),
                'price_minor' => $request->integer('price_minor'),
                'currency' => $request->string('currency')->toString(),
                'sort_order' => (int) $request->input('sort_order', 0),
                'is_active' => (bool) $request->input('is_active', true),
            ])->save();

            $this->syncChildren($comboPackage, $request->validated());
        });

        return response()->json(['data' => ['id' => $comboPackage->id]]);
    }

    public function deactivate(Store $store, ComboPackage $comboPackage): JsonResponse
    {
        abort_unless($comboPackage->merchant_id === $store->merchant_id && $comboPackage->store_id === $store->id, 404);
        $comboPackage->forceFill(['is_active' => false])->save();

        return response()->json(['data' => ['id' => $comboPackage->id, 'is_active' => false]]);
    }

    /**
     * @param array<string,mixed> $validated
     */
    private function syncChildren(ComboPackage $comboPackage, array $validated): void
    {
        ComboPackageItem::query()->where('combo_package_id', $comboPackage->id)->delete();
        ComboPackageAddOn::query()->where('combo_package_id', $comboPackage->id)->delete();

        foreach ($validated['items'] as $item) {
            ComboPackageItem::query()->create([
                'merchant_id' => $comboPackage->merchant_id,
                'combo_package_id' => $comboPackage->id,
                'catalog_item_id' => (string) $item['catalog_item_id'],
                'quantity' => (int) $item['quantity'],
            ]);
        }

        foreach (($validated['add_on_item_ids'] ?? []) as $addOnItemId) {
            ComboPackageAddOn::query()->create([
                'merchant_id' => $comboPackage->merchant_id,
                'combo_package_id' => $comboPackage->id,
                'catalog_item_id' => (string) $addOnItemId,
            ]);
        }
    }

    /**
     * @param array<int,string> $catalogItemIds
     */
    private function assertCatalogItemsInStoreMerchant(Store $store, array $catalogItemIds): void
    {
        $catalogItemIds = array_values(array_unique($catalogItemIds));
        if ($catalogItemIds === []) {
            return;
        }

        $count = CatalogItem::query()
            ->where('merchant_id', $store->merchant_id)
            ->whereIn('id', $catalogItemIds)
            ->count();

        abort_unless($count === count($catalogItemIds), 404);
    }
}
