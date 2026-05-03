<?php

namespace App\Modules\SalonWorkforce\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;

class ListStaffForDevice
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function handle(Device $device): array
    {
        return StaffProfile::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('status', 'active')
            ->orderBy('display_name')
            ->get()
            ->map(fn (StaffProfile $staff): array => [
                'id' => $staff->id,
                'display_name' => $staff->display_name,
                'code' => $staff->code,
                'role_title' => $staff->role_title,
                'status' => $staff->status,
            ])->values()->all();
    }
}
