<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Domain\Models\ModifierGroup;
use App\Modules\Catalog\Domain\Models\ModifierOption;
use App\Modules\Catalog\Interfaces\Http\Requests\UpsertModifierOptionRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;

class AdminModifierOptionController extends Controller
{
    public function store(UpsertModifierOptionRequest $request, Store $store): JsonResponse
    {
        $modifierGroup = ModifierGroup::query()
            ->whereKey($request->string('modifier_group_id')->toString())
            ->where('merchant_id', $store->merchant_id)
            ->where(function ($query) use ($store): void {
                $query->whereNull('store_id')->orWhere('store_id', $store->id);
            })
            ->first();
        abort_unless($modifierGroup !== null, 404);

        $option = ModifierOption::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'modifier_group_id' => $modifierGroup->id,
            'name' => $request->string('name')->toString(),
            'code' => $request->input('code'),
            'price_delta_minor' => (int) $request->input('price_delta_minor', 0),
            'currency' => $request->input('currency'),
            'sort_order' => (int) $request->input('sort_order', 0),
            'is_active' => (bool) $request->input('is_active', true),
        ]);

        return response()->json(['data' => ['id' => $option->id]], 201);
    }

    public function update(UpsertModifierOptionRequest $request, Store $store, ModifierOption $modifierOption): JsonResponse
    {
        abort_unless($modifierOption->merchant_id === $store->merchant_id && $modifierOption->store_id === $store->id, 404);
        $modifierGroup = ModifierGroup::query()
            ->whereKey($request->string('modifier_group_id')->toString())
            ->where('merchant_id', $store->merchant_id)
            ->where(function ($query) use ($store): void {
                $query->whereNull('store_id')->orWhere('store_id', $store->id);
            })
            ->first();
        abort_unless($modifierGroup !== null, 404);

        $modifierOption->forceFill([
            'modifier_group_id' => $modifierGroup->id,
            'name' => $request->string('name')->toString(),
            'code' => $request->input('code'),
            'price_delta_minor' => (int) $request->input('price_delta_minor', 0),
            'currency' => $request->input('currency'),
            'sort_order' => (int) $request->input('sort_order', 0),
            'is_active' => (bool) $request->input('is_active', true),
        ])->save();

        return response()->json(['data' => ['id' => $modifierOption->id]]);
    }

    public function deactivate(Store $store, ModifierOption $modifierOption): JsonResponse
    {
        abort_unless($modifierOption->merchant_id === $store->merchant_id && $modifierOption->store_id === $store->id, 404);
        $modifierOption->forceFill(['is_active' => false])->save();

        return response()->json(['data' => ['id' => $modifierOption->id, 'is_active' => false]]);
    }
}
