<?php

namespace App\Platform\Http\Middleware;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use Closure;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureStoreRole
{
    public function handle(Request $request, Closure $next, string $permission): Response
    {
        $user = $request->user();
        $store = $request->route('store');

        if (is_string($store)) {
            $store = Store::query()->find($store);
        }

        if (! $user instanceof User || ! $store instanceof Store) {
            return new JsonResponse(['message' => 'Store context is required.'], Response::HTTP_FORBIDDEN);
        }

        if (! $user->is_super_admin && (! $user->accessibleStoreIds()->contains($store->id) || ! $user->distinctPermissionKeys()->contains($permission))) {
            return new JsonResponse(['message' => 'This action is not permitted for your role.'], Response::HTTP_FORBIDDEN);
        }

        return $next($request);
    }
}
