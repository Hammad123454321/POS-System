<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Queries\ShowPayrollSnapshot;
use App\Modules\SalonWorkforce\Domain\Models\PayrollSnapshot;
use DomainException;
use Illuminate\Http\JsonResponse;

class AdminPayrollSnapshotController extends Controller
{
    public function show(
        Store $store,
        PayrollSnapshot $payrollSnapshot,
        ShowPayrollSnapshot $query,
    ): JsonResponse {
        try {
            return response()->json([
                'data' => $query->handle($store, $payrollSnapshot),
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
