<?php

namespace App\Modules\Reporting\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Reporting\Application\Queries\RetailStockMovementSummaryQuery;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminRetailStockMovementSummaryController extends Controller
{
    public function __invoke(Request $request, Store $store, RetailStockMovementSummaryQuery $query): JsonResponse
    {
        return response()->json([
            'data' => $query->handle(
                store: $store,
                from: $request->query('from'),
                to: $request->query('to'),
            ),
        ]);
    }
}
