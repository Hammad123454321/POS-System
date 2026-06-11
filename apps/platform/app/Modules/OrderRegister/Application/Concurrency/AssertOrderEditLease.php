<?php

namespace App\Modules\OrderRegister\Application\Concurrency;

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\PlatformCore\Application\Concurrency\EditLeaseException;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use Carbon\CarbonImmutable;

/**
 * Order-mutation guard. An absent lease is allowed (single-device flows keep
 * working — the lease is required only to win contention, matching table
 * semantics). A LIVE lease held by a different device throws 409.
 */
class AssertOrderEditLease
{
    public const RESOURCE_TYPE = 'order_edit';

    public function handle(Device $device, Order $order): void
    {
        $lease = EditLease::query()
            ->where('resource_type', self::RESOURCE_TYPE)
            ->where('resource_id', $order->id)
            ->first();

        if ($lease === null || $lease->holder_device_id === null) {
            return;
        }

        $now = CarbonImmutable::now('UTC');

        // Expired lease is treated as absent.
        if ($lease->lease_expires_at !== null && $lease->lease_expires_at->lte($now)) {
            return;
        }

        if ($lease->holder_device_id !== $device->id) {
            throw new EditLeaseException(
                'Another device currently holds the edit lease for this order.',
                'LEASE_CONFLICT',
                409,
                $lease->lease_version,
                $lease->holder_device_id,
                $lease->lease_expired_at?->toIso8601String(),
                $lease->lease_expires_at?->toIso8601String(),
            );
        }
    }
}
