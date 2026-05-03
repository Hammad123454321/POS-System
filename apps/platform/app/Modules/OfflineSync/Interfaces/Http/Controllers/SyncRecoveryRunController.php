<?php

namespace App\Modules\OfflineSync\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OfflineSync\Application\Actions\StartSyncRecoveryRun;
use App\Modules\OfflineSync\Application\Queries\DescribeSyncRecoveryRun;
use App\Modules\OfflineSync\Domain\Models\SyncRecoveryRun;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class SyncRecoveryRunController extends Controller
{
    public function store(Request $request, StartSyncRecoveryRun $action): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json([
            'data' => $action->handle($device),
        ], 202);
    }

    public function show(Request $request, SyncRecoveryRun $syncRecoveryRun, DescribeSyncRecoveryRun $query): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $query->handle($device, $syncRecoveryRun),
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
