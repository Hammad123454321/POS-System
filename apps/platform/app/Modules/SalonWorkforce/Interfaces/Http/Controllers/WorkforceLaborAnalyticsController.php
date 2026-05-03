<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Application\Queries\LaborAnalyticsQuery;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\LaborAnalyticsRangeRequest;
use Carbon\CarbonImmutable;
use Illuminate\Http\JsonResponse;

class WorkforceLaborAnalyticsController extends Controller
{
    public function __invoke(
        LaborAnalyticsRangeRequest $request,
        LaborAnalyticsQuery $query,
    ): JsonResponse {
        /** @var Device $device */
        $device = $request->user();
        $store = $device->store()->firstOrFail();

        $start = $request->filled('start_date')
            ? CarbonImmutable::parse((string) $request->input('start_date'), $store->timezone)
            : CarbonImmutable::now($store->timezone)->startOfWeek(CarbonImmutable::MONDAY);
        $end = $request->filled('end_date')
            ? CarbonImmutable::parse((string) $request->input('end_date'), $store->timezone)
            : $start->addDays(6);

        return response()->json([
            'data' => $query->handle(
                $store,
                $start->startOfDay(),
                $end->endOfDay(),
            ),
        ]);
    }
}
