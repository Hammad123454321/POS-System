<?php

namespace App\Modules\StoredValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Application\Queries\LookupMembershipForDevice;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class MembershipLookupController extends Controller
{
    public function __invoke(Request $request, LookupMembershipForDevice $query): JsonResponse
    {
        $validated = $request->validate([
            'member_number' => ['nullable', 'string', 'max:64', 'required_without:customer_id'],
            'customer_id' => ['nullable', 'string', 'max:64', 'required_without:member_number'],
        ]);

        /** @var Device $device */
        $device = $request->user();
        $membership = $query->handle(
            $device,
            $validated['member_number'] ?? null,
            $validated['customer_id'] ?? null,
        );

        if ($membership === null) {
            return response()->json([
                'message' => 'No active membership matched the requested lookup.',
            ], 404);
        }

        return response()->json([
            'data' => $membership,
        ]);
    }
}
