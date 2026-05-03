<?php

namespace App\Modules\PlatformCore\Application\Concurrency;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;

class AcquireEditLease
{
    public function handle(
        Device $device,
        string $resourceType,
        string $resourceId,
        int $ttlSeconds,
    ): EditLease {
        return DB::transaction(function () use ($device, $resourceId, $resourceType, $ttlSeconds): EditLease {
            $now = CarbonImmutable::now('UTC');

            $lease = EditLease::query()
                ->where('resource_type', $resourceType)
                ->where('resource_id', $resourceId)
                ->lockForUpdate()
                ->first();

            if ($lease === null) {
                return EditLease::query()->create([
                    'merchant_id' => $device->merchant_id,
                    'store_id' => $device->store_id,
                    'resource_type' => $resourceType,
                    'resource_id' => $resourceId,
                    'holder_device_id' => $device->id,
                    'ttl_seconds' => $ttlSeconds,
                    'last_heartbeat_at' => $now,
                    'lease_expires_at' => $now->addSeconds($ttlSeconds),
                ])->refresh();
            }

            if ($this->isExpired($lease, $now)) {
                $lease->forceFill([
                    'holder_device_id' => null,
                    'lease_version' => $lease->lease_version + 1,
                    'lease_expired_at' => $now,
                    'last_heartbeat_at' => null,
                    'lease_expires_at' => null,
                ])->save();
            }

            if ($lease->holder_device_id !== null && $lease->holder_device_id !== $device->id) {
                throw new EditLeaseException(
                    'Another device currently holds the edit lease for this resource.',
                    'LEASE_CONFLICT',
                    409,
                    $lease->lease_version,
                    $lease->holder_device_id,
                    $lease->lease_expired_at?->toIso8601String(),
                    $lease->lease_expires_at?->toIso8601String(),
                );
            }

            $lease->forceFill([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'holder_device_id' => $device->id,
                'ttl_seconds' => $ttlSeconds,
                'last_heartbeat_at' => $now,
                'lease_expires_at' => $now->addSeconds($ttlSeconds),
            ])->save();

            return $lease->refresh();
        });
    }

    private function isExpired(EditLease $lease, CarbonImmutable $now): bool
    {
        return $lease->holder_device_id !== null
            && $lease->lease_expires_at !== null
            && $lease->lease_expires_at->lte($now);
    }
}
