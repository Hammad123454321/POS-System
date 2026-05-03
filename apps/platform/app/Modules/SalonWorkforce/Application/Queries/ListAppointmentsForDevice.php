<?php

namespace App\Modules\SalonWorkforce\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use Carbon\CarbonImmutable;

class ListAppointmentsForDevice
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function handle(
        Device $device,
        ?string $businessDateFrom = null,
        ?string $businessDateTo = null,
    ): array {
        $from = $businessDateFrom === null
            ? CarbonImmutable::now($device->store?->timezone ?? 'UTC')->toDateString()
            : CarbonImmutable::parse($businessDateFrom)->toDateString();
        $to = $businessDateTo === null
            ? $from
            : CarbonImmutable::parse($businessDateTo)->toDateString();

        return Appointment::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->whereBetween('business_date', [$from, $to])
            ->with('staffProfile')
            ->orderBy('starts_at')
            ->get()
            ->map(function (Appointment $appointment): array {
                /** @var StaffProfile|null $staff */
                $staff = $appointment->staffProfile;

                return [
                    'id' => $appointment->id,
                    'status' => $appointment->status,
                    'status_seq' => $appointment->status_seq,
                    'business_date' => $appointment->business_date?->format('Y-m-d'),
                    'staff_profile_id' => $appointment->staff_profile_id,
                    'staff_display_name' => $staff?->display_name,
                    'service_item_id' => $appointment->service_item_id,
                    'service_name' => $appointment->service_name,
                    'service_price_minor' => $appointment->service_price_minor,
                    'discount_minor' => $appointment->discount_minor,
                    'currency' => $appointment->currency,
                    'starts_at' => $appointment->starts_at?->toIso8601String(),
                    'ends_at' => $appointment->ends_at?->toIso8601String(),
                    'checked_in_at' => $appointment->checked_in_at?->toIso8601String(),
                    'completed_at' => $appointment->completed_at?->toIso8601String(),
                    'source' => $appointment->source,
                ];
            })->values()->all();
    }
}
