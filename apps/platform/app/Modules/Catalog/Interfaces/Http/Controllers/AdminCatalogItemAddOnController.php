<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\CatalogItemAddOn;
use App\Modules\Catalog\Interfaces\Http\Requests\SyncCatalogItemAddOnsRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

class AdminCatalogItemAddOnController extends Controller
{
    public function store(SyncCatalogItemAddOnsRequest $request, Store $store, CatalogItem $catalogItem): JsonResponse
    {
        abort_unless($catalogItem->merchant_id === $store->merchant_id, 404);
        $addOnItemIds = collect($request->input('add_on_item_ids', []))
            ->map(fn ($id) => (string) $id)
            ->values()
            ->all();
        abort_unless(! in_array($catalogItem->id, $addOnItemIds, true), 422);

        $count = CatalogItem::query()
            ->where('merchant_id', $store->merchant_id)
            ->whereIn('id', $addOnItemIds)
            ->count();
        abort_unless($count === count(array_unique($addOnItemIds)), 404);

        DB::transaction(function () use ($catalogItem, $request): void {
            CatalogItemAddOn::query()
                ->where('merchant_id', $catalogItem->merchant_id)
                ->where('catalog_item_id', $catalogItem->id)
                ->delete();

            foreach ($request->input('add_on_item_ids', []) as $addOnItemId) {
                CatalogItemAddOn::query()->create([
                    'merchant_id' => $catalogItem->merchant_id,
                    'catalog_item_id' => $catalogItem->id,
                    'add_on_item_id' => (string) $addOnItemId,
                ]);
            }
        });

        return response()->json(['data' => ['catalog_item_id' => $catalogItem->id]], 201);
    }
}
