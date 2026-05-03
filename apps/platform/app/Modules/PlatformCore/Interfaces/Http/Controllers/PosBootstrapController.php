<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Bootstrap\PosBootstrapService;
use App\Modules\PlatformCore\Domain\Models\Device;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class PosBootstrapController extends Controller
{
    public function __invoke(Request $request, PosBootstrapService $service): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json($service->build($device));
    }
}
