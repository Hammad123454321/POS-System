<?php

namespace App\Modules\Restaurant\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Concurrency\EditLeaseException;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Restaurant\Application\Actions\ClaimDiningTable;
use App\Modules\Restaurant\Application\Actions\HeartbeatDiningTableLease;
use App\Modules\Restaurant\Application\Actions\ReleaseDiningTable;
use App\Modules\Restaurant\Application\Queries\ListDiningTablesForDevice;
use App\Modules\Restaurant\Domain\Models\DiningTable;
use App\Modules\Restaurant\Interfaces\Http\Requests\ClaimDiningTableRequest;
use DomainException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DiningTableController extends Controller
{
    public function index(Request $request, ListDiningTablesForDevice $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json($query->handle($device));
    }

    public function claim(
        ClaimDiningTableRequest $request,
        DiningTable $diningTable,
        ClaimDiningTable $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    $device,
                    $diningTable,
                    $request->input('current_party_name'),
                    $request->filled('guest_count') ? (int) $request->integer('guest_count') : null,
                ),
            ]);
        } catch (EditLeaseException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
                'lease_version' => $exception->leaseVersion,
                'current_holder_device_id' => $exception->currentHolderDeviceId,
                'lease_expired_at' => $exception->leaseExpiredAt,
                'lease_expires_at' => $exception->leaseExpiresAt,
            ], $exception->status);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function heartbeat(
        Request $request,
        DiningTable $diningTable,
        HeartbeatDiningTableLease $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle($device, $diningTable),
            ]);
        } catch (EditLeaseException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
                'lease_version' => $exception->leaseVersion,
                'current_holder_device_id' => $exception->currentHolderDeviceId,
                'lease_expired_at' => $exception->leaseExpiredAt,
                'lease_expires_at' => $exception->leaseExpiresAt,
            ], $exception->status);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function release(
        Request $request,
        DiningTable $diningTable,
        ReleaseDiningTable $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle($device, $diningTable),
            ]);
        } catch (EditLeaseException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
                'lease_version' => $exception->leaseVersion,
                'current_holder_device_id' => $exception->currentHolderDeviceId,
                'lease_expired_at' => $exception->leaseExpiredAt,
                'lease_expires_at' => $exception->leaseExpiresAt,
            ], $exception->status);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
