<?php

namespace App\Modules\Reporting\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Reporting\Application\Queries\BusinessDaySummaryQuery;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class BusinessDaySummaryController extends Controller
{
    public function __invoke(Request $request, BusinessDaySummaryQuery $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json($query->handle($device));
    }
}
