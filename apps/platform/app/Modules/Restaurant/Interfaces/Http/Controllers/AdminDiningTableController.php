<?php

namespace App\Modules\Restaurant\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Restaurant\Application\Actions\CreateDiningTable;
use App\Modules\Restaurant\Interfaces\Http\Requests\CreateDiningTableRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminDiningTableController extends Controller
{
    public function __invoke(
        CreateDiningTableRequest $request,
        Store $store,
        CreateDiningTable $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create dining tables.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('name')->toString(),
                    $request->input('zone_name'),
                    (int) $request->integer('capacity'),
                    (int) $request->integer('sort_order', 0),
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
