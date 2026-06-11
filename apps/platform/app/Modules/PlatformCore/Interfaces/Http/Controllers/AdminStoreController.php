<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Onboarding\CreateStore;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Interfaces\Http\Requests\CreateStoreRequest;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\Gate;

class AdminStoreController extends Controller
{
    public function index(Merchant $merchant): JsonResponse
    {
        Gate::authorize('manageStores', $merchant);

        $stores = $merchant->stores()
            ->orderBy('name')
            ->get(['id', 'name', 'code', 'mode', 'timezone', 'business_day_cutoff', 'status']);

        return response()->json(['data' => $stores]);
    }

    public function store(CreateStoreRequest $request, Merchant $merchant, CreateStore $action): JsonResponse
    {
        Gate::authorize('manageStores', $merchant);

        $store = $action->handle(
            $merchant,
            $request->string('name')->toString(),
            $request->input('code'),
            $request->string('mode')->toString(),
            $request->string('timezone')->toString(),
            $request->string('business_day_cutoff')->toString(),
        );

        return response()->json(['data' => $store->only([
            'id', 'name', 'code', 'mode', 'timezone', 'business_day_cutoff', 'status',
        ])], 201);
    }
}
