<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\Variant;
use App\Modules\Catalog\Interfaces\Http\Requests\UpsertVariantRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;

class AdminVariantController extends Controller
{
    public function store(UpsertVariantRequest $request, Store $store): JsonResponse
    {
        $catalogItem = CatalogItem::query()
            ->whereKey($request->string('catalog_item_id')->toString())
            ->where('merchant_id', $store->merchant_id)
            ->first();
        abort_unless($catalogItem !== null, 404);

        $variant = Variant::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'catalog_item_id' => $catalogItem->id,
            'name' => $request->string('name')->toString(),
            'code' => $request->input('code'),
            'options' => $request->input('options'),
            'price_delta_minor' => $request->integer('price_delta_minor'),
            'currency' => $request->input('currency'),
            'sort_order' => (int) $request->input('sort_order', 0),
            'is_active' => (bool) $request->input('is_active', true),
        ]);

        return response()->json(['data' => ['id' => $variant->id]], 201);
    }

    public function update(UpsertVariantRequest $request, Store $store, Variant $variant): JsonResponse
    {
        abort_unless($variant->merchant_id === $store->merchant_id && $variant->store_id === $store->id, 404);
        $catalogItem = CatalogItem::query()
            ->whereKey($request->string('catalog_item_id')->toString())
            ->where('merchant_id', $store->merchant_id)
            ->first();
        abort_unless($catalogItem !== null, 404);

        $variant->forceFill([
            'catalog_item_id' => $catalogItem->id,
            'name' => $request->string('name')->toString(),
            'code' => $request->input('code'),
            'options' => $request->input('options'),
            'price_delta_minor' => $request->integer('price_delta_minor'),
            'currency' => $request->input('currency'),
            'sort_order' => (int) $request->input('sort_order', 0),
            'is_active' => (bool) $request->input('is_active', true),
        ])->save();

        return response()->json(['data' => ['id' => $variant->id]]);
    }

    public function deactivate(Store $store, Variant $variant): JsonResponse
    {
        abort_unless($variant->merchant_id === $store->merchant_id && $variant->store_id === $store->id, 404);
        $variant->forceFill(['is_active' => false])->save();

        return response()->json(['data' => ['id' => $variant->id, 'is_active' => false]]);
    }
}
