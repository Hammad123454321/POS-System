<?php

namespace App\Modules\Catalog\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\Catalog\Application\Actions\CreateDiscountRule;
use App\Modules\Catalog\Interfaces\Http\Requests\CreateDiscountRuleRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminDiscountRuleController extends Controller
{
    public function __invoke(
        CreateDiscountRuleRequest $request,
        Store $store,
        CreateDiscountRule $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create discounts.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('name')->toString(),
                    $request->filled('code') ? $request->string('code')->toString() : null,
                    $request->string('type')->toString(),
                    $request->filled('value_minor') ? (int) $request->integer('value_minor') : null,
                    $request->filled('value_basis_points') ? (int) $request->integer('value_basis_points') : null,
                    (int) $request->integer('sort_order', 0),
                    $request->filled('scope_mode') ? $request->string('scope_mode')->toString() : 'all',
                    $request->input('applicability'),
                    $request->boolean('is_stackable'),
                    $request->filled('starts_at') ? $request->string('starts_at')->toString() : null,
                    $request->filled('ends_at') ? $request->string('ends_at')->toString() : null,
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
