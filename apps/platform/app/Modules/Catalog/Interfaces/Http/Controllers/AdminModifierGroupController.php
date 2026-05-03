<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\ModifierGroup;
use App\Modules\Catalog\Interfaces\Http\Requests\UpsertModifierGroupRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;

class AdminModifierGroupController extends Controller
{
    public function store(UpsertModifierGroupRequest $request, Store $store): JsonResponse
    {
        $catalogItem = CatalogItem::query()
            ->whereKey($request->string('catalog_item_id')->toString())
            ->where('merchant_id', $store->merchant_id)
            ->first();
        abort_unless($catalogItem !== null, 404);

        $group = ModifierGroup::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'catalog_item_id' => $catalogItem->id,
            'name' => $request->string('name')->toString(),
            'selection_mode' => $request->string('selection_mode')->toString(),
            'min_select' => (int) $request->input('min_select', 0),
            'max_select' => $request->input('max_select'),
            'is_required' => (bool) $request->input('is_required', false),
            'is_active' => (bool) $request->input('is_active', true),
            'sort_order' => (int) $request->input('sort_order', 0),
        ]);

        return response()->json(['data' => ['id' => $group->id]], 201);
    }

    public function update(UpsertModifierGroupRequest $request, Store $store, ModifierGroup $modifierGroup): JsonResponse
    {
        abort_unless($modifierGroup->merchant_id === $store->merchant_id && $modifierGroup->store_id === $store->id, 404);
        $catalogItem = CatalogItem::query()
            ->whereKey($request->string('catalog_item_id')->toString())
            ->where('merchant_id', $store->merchant_id)
            ->first();
        abort_unless($catalogItem !== null, 404);

        $modifierGroup->forceFill([
            'catalog_item_id' => $catalogItem->id,
            'name' => $request->string('name')->toString(),
            'selection_mode' => $request->string('selection_mode')->toString(),
            'min_select' => (int) $request->input('min_select', 0),
            'max_select' => $request->input('max_select'),
            'is_required' => (bool) $request->input('is_required', false),
            'is_active' => (bool) $request->input('is_active', true),
            'sort_order' => (int) $request->input('sort_order', 0),
        ])->save();

        return response()->json(['data' => ['id' => $modifierGroup->id]]);
    }

    public function deactivate(Store $store, ModifierGroup $modifierGroup): JsonResponse
    {
        abort_unless($modifierGroup->merchant_id === $store->merchant_id && $modifierGroup->store_id === $store->id, 404);
        $modifierGroup->forceFill(['is_active' => false])->save();

        return response()->json(['data' => ['id' => $modifierGroup->id, 'is_active' => false]]);
    }
}
