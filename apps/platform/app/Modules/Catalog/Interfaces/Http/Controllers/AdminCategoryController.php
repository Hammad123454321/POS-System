<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Domain\Models\Category;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class AdminCategoryController extends Controller
{
    public function index(Store $store): JsonResponse
    {
        Gate::authorize('manageCatalog', $store);

        $categories = Category::query()
            ->where('store_id', $store->id)
            ->orderBy('sort_order')
            ->get(['id', 'name', 'sort_order', 'is_active']);

        return response()->json(['data' => $categories]);
    }

    public function store(Request $request, Store $store): JsonResponse
    {
        Gate::authorize('manageCatalog', $store);

        $validated = $request->validate([
            'name' => ['required', 'string', 'max:120'],
            'sort_order' => ['nullable', 'integer', 'min:0'],
        ]);

        $category = Category::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'name' => $validated['name'],
            'sort_order' => $validated['sort_order'] ?? 0,
            'is_active' => true,
        ]);

        return response()->json(['data' => $category->only(['id', 'name', 'sort_order', 'is_active'])], 201);
    }

    public function update(Request $request, Store $store, Category $category): JsonResponse
    {
        Gate::authorize('manageCatalog', $store);

        abort_unless($category->store_id === $store->id, 404);

        $validated = $request->validate([
            'name' => ['sometimes', 'string', 'max:120'],
            'sort_order' => ['sometimes', 'integer', 'min:0'],
        ]);

        $category->fill($validated)->save();

        return response()->json(['data' => $category->only(['id', 'name', 'sort_order', 'is_active'])]);
    }

    public function deactivate(Store $store, Category $category): JsonResponse
    {
        Gate::authorize('manageCatalog', $store);

        abort_unless($category->store_id === $store->id, 404);

        $category->forceFill(['is_active' => false])->save();

        return response()->json(['data' => $category->only(['id', 'name', 'sort_order', 'is_active'])]);
    }
}
