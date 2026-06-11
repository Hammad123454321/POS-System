<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OrderRegister\Application\Concurrency\AssertOrderEditLease;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\PlatformCore\Application\Concurrency\AcquireEditLease;
use App\Modules\PlatformCore\Application\Concurrency\EditLeaseException;
use App\Modules\PlatformCore\Application\Concurrency\HeartbeatEditLease;
use App\Modules\PlatformCore\Application\Concurrency\ReleaseEditLease;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class OrderEditLeaseController extends Controller
{
    public function claim(Request $request, Order $order, AcquireEditLease $action): JsonResponse
    {
        return $this->guard(fn () => $action->handle(
            $this->device($request),
            AssertOrderEditLease::RESOURCE_TYPE,
            $order->id,
            (int) config('pos.leases.order_edit_ttl_seconds', 45),
        ));
    }

    public function heartbeat(Request $request, Order $order, HeartbeatEditLease $action): JsonResponse
    {
        return $this->guard(fn () => $action->handle(
            $this->device($request),
            AssertOrderEditLease::RESOURCE_TYPE,
            $order->id,
        ));
    }

    public function release(Request $request, Order $order, ReleaseEditLease $action): JsonResponse
    {
        return $this->guard(fn () => $action->handle(
            $this->device($request),
            AssertOrderEditLease::RESOURCE_TYPE,
            $order->id,
        ));
    }

    private function device(Request $request): Device
    {
        /** @var Device $device */
        $device = $request->user();

        return $device;
    }

    private function guard(callable $callback): JsonResponse
    {
        try {
            /** @var EditLease $lease */
            $lease = $callback();

            return response()->json(['data' => [
                'resource_id' => $lease->resource_id,
                'lease_version' => $lease->lease_version,
                'holder_device_id' => $lease->holder_device_id,
                'lease_expires_at' => $lease->lease_expires_at?->toIso8601String(),
            ]]);
        } catch (EditLeaseException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
                'lease_version' => $exception->leaseVersion,
                'current_holder_device_id' => $exception->currentHolderDeviceId,
                'lease_expired_at' => $exception->leaseExpiredAt,
                'lease_expires_at' => $exception->leaseExpiresAt,
            ], $exception->status);
        }
    }
}
