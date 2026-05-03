<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Modules\PlatformCore\Application\Concurrency\HeartbeatEditLease;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use DomainException;

class HeartbeatAppointmentSlotClaim
{
    public function __construct(
        private readonly HeartbeatEditLease $heartbeatEditLease,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, EditLease $slotClaim): array
    {
        if ($slotClaim->resource_type !== 'slot_claim') {
            throw new DomainException('Lease resource is not a slot claim.');
        }

        if ($slotClaim->merchant_id !== $device->merchant_id || $slotClaim->store_id !== $device->store_id) {
            throw new DomainException('Slot claim does not belong to this device store.');
        }

        $lease = $this->heartbeatEditLease->handle(
            $device,
            $slotClaim->resource_type,
            $slotClaim->resource_id,
        );

        return [
            'lease_id' => $lease->id,
            'lease_version' => $lease->lease_version,
            'current_holder_device_id' => $lease->holder_device_id,
            'lease_expires_at' => $lease->lease_expires_at?->toIso8601String(),
        ];
    }
}
