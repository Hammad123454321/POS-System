<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Actions\GenerateWeeklyPayrollSnapshot;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\GeneratePayrollSnapshotRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminGeneratePayrollSnapshotController extends Controller
{
    public function __invoke(
        GeneratePayrollSnapshotRequest $request,
        Store $store,
        GenerateWeeklyPayrollSnapshot $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may generate payroll snapshots.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->filled('week_reference_date')
                        ? $request->date('week_reference_date')?->format('Y-m-d')
                        : null,
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
