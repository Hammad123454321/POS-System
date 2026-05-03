<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Actions\UpsertStaffServiceRule;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\UpsertStaffServiceRuleRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminStaffServiceRuleController extends Controller
{
    public function __invoke(
        UpsertStaffServiceRuleRequest $request,
        Store $store,
        UpsertStaffServiceRule $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may assign staff services.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('staff_profile_id')->toString(),
                    $request->string('service_item_id')->toString(),
                    $request->filled('commission_rule_id') ? $request->string('commission_rule_id')->toString() : null,
                    (bool) $request->boolean('is_active', true),
                ),
            ], 200);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
