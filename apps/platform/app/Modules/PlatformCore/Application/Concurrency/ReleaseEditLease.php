<?php

namespace App\Modules\PlatformCore\Application\Concurrency;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;

class ReleaseEditLease
{
    public function handle(Device $device, string $resourceType, string $resourceId): EditLease
    {
        return DB::transaction(function () use ($device, $resourceId, $resourceType): EditLease {
            $now = CarbonImmutable::now('UTC');

            $lease = EditLease::query()
                ->where('resource_type', $resourceType)
                ->where('resource_id', $resourceId)
                ->lockForUpdate()
                ->first();

            if ($lease === null || $lease->holder_device_id === null) {
                throw new EditLeaseException(
                    'The edit lease has already expired.',
                    'LEASE_EXPIRED',
                    409,
                    $lease?->lease_version,
                    $lease?->holder_device_id,
                    $lease?->lease_expired_at?->toIso8601String(),
                    $lease?->lease_expires_at?->toIso8601String(),
                );
            }

            if ($lease->lease_expires_at !== null && $lease->lease_expires_at->lte($now)) {
                $lease->forceFill([
                    'holder_device_id' => null,
                    'lease_version' => $lease->lease_version + 1,
                    'lease_expired_at' => $now,
                    'last_heartbeat_at' => null,
                    'lease_expires_at' => null,
                ])->save();

                throw new EditLeaseException(
                    'The edit lease expired before the release request reached the server.',
                    'LEASE_EXPIRED',
                    409,
                    $lease->lease_version,
                    null,
                    $lease->lease_expired_at?->toIso8601String(),
                    $lease->lease_expires_at?->toIso8601String(),
                );
            }

            if ($lease->holder_device_id !== $device->id) {
                throw new EditLeaseException(
                    'Another device owns this edit lease.',
                    'LEASE_EXPIRED',
                    409,
                    $lease->lease_version,
                    $lease->holder_device_id,
                    $lease->lease_expired_at?->toIso8601String(),
                    $lease->lease_expires_at?->toIso8601String(),
                );
            }

            $lease->forceFill([
                'holder_device_id' => null,
                'lease_version' => $lease->lease_version + 1,
                'last_heartbeat_at' => null,
                'lease_expires_at' => null,
            ])->save();

            return $lease->refresh();
        });
    }
}
