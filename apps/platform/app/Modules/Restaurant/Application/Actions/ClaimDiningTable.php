<?php

namespace App\Modules\Restaurant\Application\Actions;

use App\Modules\PlatformCore\Application\Concurrency\AcquireEditLease;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Restaurant\Domain\Models\DiningTable;
use App\Modules\Restaurant\Domain\Models\TableAssignment;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;

class ClaimDiningTable
{
    public function __construct(
        private readonly AcquireEditLease $acquireEditLease,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        Device $device,
        DiningTable $diningTable,
        ?string $currentPartyName,
        ?int $guestCount,
    ): array {
        if ($diningTable->merchant_id !== $device->merchant_id || $diningTable->store_id !== $device->store_id) {
            throw new DomainException('The requested dining table does not belong to this device context.');
        }

        if (! $diningTable->is_active) {
            throw new DomainException('Only active dining tables can be claimed.');
        }

        $lease = $this->acquireEditLease->handle(
            $device,
            'table_assignment',
            $diningTable->id,
            (int) config('pos.leases.table_assignment_ttl_seconds', 30),
        );

        /** @var TableAssignment $assignment */
        $assignment = DB::transaction(function () use ($currentPartyName, $device, $diningTable, $guestCount): TableAssignment {
            $assignment = TableAssignment::query()
                ->where('dining_table_id', $diningTable->id)
                ->lockForUpdate()
                ->firstOrFail();

            if ($assignment->status === 'available') {
                $assignment->forceFill([
                    'status' => 'occupied',
                    'current_party_name' => $currentPartyName ?: 'Walk-in',
                    'guest_count' => $guestCount,
                    'assigned_device_id' => $device->id,
                    'occupied_at' => CarbonImmutable::now('UTC'),
                    'released_at' => null,
                ])->save();
            } else {
                $assignment->forceFill([
                    'assigned_device_id' => $device->id,
                ])->save();
            }

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
