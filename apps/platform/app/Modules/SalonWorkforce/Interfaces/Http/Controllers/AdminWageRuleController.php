<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Actions\CreateWageRule;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\CreateWageRuleRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminWageRuleController extends Controller
{
    public function __invoke(
        CreateWageRuleRequest $request,
        Store $store,
        CreateWageRule $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create wage rules.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('staff_profile_id')->toString(),
                    $request->string('name')->toString(),
                    (int) $request->integer('hourly_rate_minor'),
                    strtoupper($request->string('currency', 'USD')->toString()),
                    $request->date('effective_from')?->format('Y-m-d') ?? now()->toDateString(),
                    $request->filled('effective_to') ? $request->date('effective_to')?->format('Y-m-d') : null,
                    (bool) $request->boolean('is_active', true),
                    $request->filled('metadata') ? $request->input('metadata') : null,
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
