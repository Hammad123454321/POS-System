<?php

namespace App\Modules\Restaurant\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use App\Modules\Restaurant\Domain\Models\DiningTable;
use App\Modules\Restaurant\Domain\Models\TableAssignment;

class ListDiningTablesForDevice
{
    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device): array
    {
        $tables = DiningTable::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('is_active', true)
            ->orderBy('sort_order')
            ->orderBy('name')
            ->get();

        $assignments = TableAssignment::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->get()
            ->keyBy('dining_table_id');

        $leases = EditLease::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('resource_type', 'table_assignment')
            ->get()
            ->keyBy('resource_id');

        return [
            'tables' => $tables->map(function (DiningTable $table) use ($assignments, $device, $leases): array {
                /** @var TableAssignment|null $assignment */
                $assignment = $assignments->get($table->id);
                /** @var EditLease|null $lease */
                $lease = $leases->get($table->id);

                return [
                    'id' => $table->id,
                    'name' => $table->name,
                    'zone_name' => $table->zone_name,
                    'capacity' => $table->capacity,
                    'status' => $assignment?->status ?? 'available',
                    'current_party_name' => $assignment?->current_party_name,
                    'guest_count' => $assignment?->guest_count,
                    'assigned_device_id' => $assignment?->assigned_device_id,
                    'lease' => [
                        'lease_version' => $lease?->lease_version,
                        'current_holder_device_id' => $lease?->holder_device_id,
                        'lease_expires_at' => $lease?->lease_expires_at?->toIso8601String(),
                        'is_claimed_by_current_device' => $lease?->holder_device_id === $device->id,
                    ],
                ];
            })->values()->all(),
        ];
    }
}
