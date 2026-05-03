<?php

namespace App\Modules\Restaurant\Application\Actions;

use App\Modules\PlatformCore\Application\Concurrency\HeartbeatEditLease;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Restaurant\Domain\Models\DiningTable;
use App\Modules\Restaurant\Domain\Models\TableAssignment;
use DomainException;

class HeartbeatDiningTableLease
{
    public function __construct(
        private readonly HeartbeatEditLease $heartbeatEditLease,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, DiningTable $diningTable): array
    {
        if ($diningTable->merchant_id !== $device->merchant_id || $diningTable->store_id !== $device->store_id) {
            throw new DomainException('The requested dining table does not belong to this device context.');
        }

        $lease = $this->heartbeatEditLease->handle($device, 'table_assignment', $diningTable->id);

        $assignment = TableAssignment::query()
            ->where('dining_table_id', $diningTable->id)
            ->firstOrFail();

        return [
            'id' => $diningTable->id,
            'name' => $diningTable->name,
            'status' => $assignment->status,
            'current_party_name' => $assignment->current_party_name,
            'guest_count' => $assignment->guest_count,
            'assigned_device_id' => $assignment->assigned_device_id,
            'lease' => [
                'lease_version' => $lease->lease_version,
                'current_holder_device_id' => $lease->holder_device_id,
                'lease_expires_at' => $lease->lease_expires_at?->toIso8601String(),
            ],
        ];
    }
}
