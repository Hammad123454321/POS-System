<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Application\Items\CreateCatalogItem;
use App\Modules\Catalog\Application\Items\DeactivateCatalogItem;
use App\Modules\Catalog\Application\Items\UpdateCatalogItem;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Interfaces\Http\Requests\StoreCatalogItemRequest;
use App\Modules\Catalog\Interfaces\Http\Requests\UpdateCatalogItemRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class AdminCatalogItemController extends Controller
{
    public function index(Request $request, Store $store): JsonResponse
    {
        Gate::authorize('manageCatalog', $store);

        $items = CatalogItem::query()
            ->where('merchant_id', $store->merchant_id)
            ->when($request->query('category_id'), fn ($q, string $id) => $q->where('category_id', $id))
            ->when($request->has('is_active'), fn ($q) => $q->where('is_active', $request->boolean('is_active')))
            ->when($request->query('q'), fn ($q, string $term) => $q->where(fn ($w) => $w
                ->where('name', 'ilike', "%{$term}%")
                ->orWhere('sku', 'ilike', "%{$term}%")))
            ->orderBy('name')
            ->paginate(min((int) $request->integer('per_page', 25), 100));

        return response()->json([
            'data' => $items->items(),
            'meta' => [
                'page' => $items->currentPage(),
                'per_page' => $items->perPage(),
                'total' => $items->total(),
            ],
        ]);
    }

    public function store(StoreCatalogItemRequest $request, Store $store, CreateCatalogItem $action): JsonResponse
    {
        Gate::authorize('manageCatalog', $store);

        $item = $action->handle($store, $request->validated());

        return response()->json(['data' => $item], 201);
    }

    public function update(UpdateCatalogItemRequest $request, Store $store, CatalogItem $catalogItem, UpdateCatalogItem $action): JsonResponse
    {
        Gate::authorize('manageCatalog', $store);

        abort_unless($catalogItem->merchant_id === $store->merchant_id, 404);

        return response()->json(['data' => $action->handle($catalogItem, $request->validated())]);
    }

    public function deactivate(Store $store, CatalogItem $catalogItem, DeactivateCatalogItem $action): JsonResponse
    {
        Gate::authorize('manageCatalog', $store);

        abort_unless($catalogItem->merchant_id === $store->merchant_id, 404);

        return response()->json(['data' => $action->handle($catalogItem)]);
    }
}
