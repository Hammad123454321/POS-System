<?php

namespace App\Modules\CustomerValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\CustomerValue\Application\Queries\SearchCustomersForDevice;
use App\Modules\PlatformCore\Domain\Models\Device;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CustomerSearchController extends Controller
{
    public function __invoke(Request $request, SearchCustomersForDevice $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json([
            'data' => $query->handle($device, $request->query('q')),
        ]);
    }
}
