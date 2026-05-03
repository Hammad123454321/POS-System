<?php

namespace App\Modules\StoredValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\StoredValue\Application\Actions\CreateMembershipPlan;
use App\Modules\StoredValue\Interfaces\Http\Requests\CreateMembershipPlanRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminMembershipPlanController extends Controller
{
    public function __invoke(
        CreateMembershipPlanRequest $request,
        Store $store,
        CreateMembershipPlan $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create membership plans.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('name')->toString(),
                    $request->filled('code') ? $request->string('code')->toString() : null,
                    (int) $request->integer('price_minor'),
                    (int) $request->integer('duration_days'),
                    $request->input('benefits_snapshot'),
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
