<?php

namespace App\Modules\Restaurant\Application\Actions;

use App\Modules\PlatformCore\Application\Concurrency\ReleaseEditLease;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Restaurant\Domain\Models\DiningTable;
use App\Modules\Restaurant\Domain\Models\TableAssignment;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;

class ReleaseDiningTable
{
    public function __construct(
        private readonly ReleaseEditLease $releaseEditLease,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, DiningTable $diningTable): array
    {
        if ($diningTable->merchant_id !== $device->merchant_id || $diningTable->store_id !== $device->store_id) {
            throw new DomainException('The requested dining table does not belong to this device context.');
        }

        $lease = $this->releaseEditLease->handle($device, 'table_assignment', $diningTable->id);

        /** @var TableAssignment $assignment */
        $assignment = DB::transaction(function () use ($device, $diningTable): TableAssignment {
            $assignment = TableAssignment::query()
                ->where('dining_table_id', $diningTable->id)
                ->lockForUpdate()
                ->firstOrFail();

            if ($assignment->status === 'available') {
                throw new DomainException('This dining table is already available.');
            }

            if ($assignment->assigned_device_id !== $device->id) {
                throw new DomainException('Only the device that currently controls the table may release it.');
            }

            $assignment->forceFill([
                'status' => 'available',
                'current_party_name' => null,
                'guest_count' => null,
                'assigned_device_id' => null,
                'released_at' => CarbonImmutable::now('UTC'),
            ])->save();

            return $assignment->refresh();
        });

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
