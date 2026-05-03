<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Actions\CreateCommissionRule;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\CreateCommissionRuleRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminCommissionRuleController extends Controller
{
    public function __invoke(
        CreateCommissionRuleRequest $request,
        Store $store,
        CreateCommissionRule $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create commission rules.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('name')->toString(),
                    $request->string('base_type')->toString(),
                    $request->filled('rate_basis_points') ? (int) $request->integer('rate_basis_points') : null,
                    $request->filled('fixed_minor') ? (int) $request->integer('fixed_minor') : null,
                    strtoupper($request->string('currency', 'USD')->toString()),
                    $request->filled('effective_from') ? $request->date('effective_from')?->format('Y-m-d') : null,
                    $request->filled('effective_to') ? $request->date('effective_to')?->format('Y-m-d') : null,
                    (bool) $request->boolean('is_active', true),
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
