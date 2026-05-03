<?php

namespace App\Modules\StoredValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Application\Actions\ActivateMembership;
use App\Modules\StoredValue\Interfaces\Http\Requests\ActivateMembershipRequest;
use DomainException;
use Illuminate\Http\JsonResponse;

class MembershipActivateController extends Controller
{
    public function __invoke(ActivateMembershipRequest $request, ActivateMembership $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    $device,
                    $request->string('customer_id')->toString(),
                    $request->string('membership_plan_id')->toString(),
                    $request->filled('member_number') ? $request->string('member_number')->toString() : null,
                ),
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
