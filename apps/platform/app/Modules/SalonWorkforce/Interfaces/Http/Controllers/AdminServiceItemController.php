<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Actions\CreateServiceItem;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\CreateServiceItemRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminServiceItemController extends Controller
{
    public function __invoke(
        CreateServiceItemRequest $request,
        Store $store,
        CreateServiceItem $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create service items.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('name')->toString(),
                    $request->filled('sku') ? $request->string('sku')->toString() : null,
                    (int) $request->integer('base_price_minor'),
                    (int) $request->integer('duration_minutes'),
                    $request->filled('buffer_minutes') ? (int) $request->integer('buffer_minutes') : 0,
                    (bool) $request->boolean('is_walk_in_enabled', true),
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
