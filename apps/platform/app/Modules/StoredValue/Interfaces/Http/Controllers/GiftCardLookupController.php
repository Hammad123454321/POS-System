<?php

namespace App\Modules\StoredValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Application\Queries\LookupGiftCardForDevice;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class GiftCardLookupController extends Controller
{
    public function __invoke(Request $request, LookupGiftCardForDevice $query): JsonResponse
    {
        $validated = $request->validate([
            'code' => ['required', 'string', 'max:64'],
        ]);

        /** @var Device $device */
        $device = $request->user();

        return response()->json([
            'data' => $query->handle($device, $validated['code']),
        ]);
    }
}
