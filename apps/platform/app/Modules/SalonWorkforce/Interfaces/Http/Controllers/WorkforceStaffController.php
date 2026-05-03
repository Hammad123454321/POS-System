<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Application\Queries\ListStaffForDevice;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class WorkforceStaffController extends Controller
{
    public function __invoke(Request $request, ListStaffForDevice $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json([
            'data' => $query->handle($device),
        ]);
    }
}
