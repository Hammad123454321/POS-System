<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Modules\PlatformCore\Application\Concurrency\AcquireEditLease;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use Carbon\CarbonImmutable;
use DomainException;

class ClaimAppointmentSlot
{
    public function __construct(
        private readonly AcquireEditLease $acquireEditLease,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        Device $device,
        string $staffProfileId,
        string $startsAt,
        string $endsAt,
    ): array {
        $staffProfile = StaffProfile::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('status', 'active')
            ->whereKey($staffProfileId)
            ->firstOrFail();

        $startsAtUtc = CarbonImmutable::parse($startsAt)->setTimezone('UTC');
        $endsAtUtc = CarbonImmutable::parse($endsAt)->setTimezone('UTC');

        if ($endsAtUtc->lte($startsAtUtc)) {
            throw new DomainException('Appointment end time must be after start time.');
        }

        $resourceId = $this->resourceId($staffProfile->id, $startsAtUtc, $endsAtUtc);
        $lease = $this->acquireEditLease->handle(
            $device,
            'slot_claim',
            $resourceId,
            60,
        );

        return [
            'lease_id' => $lease->id,
            'resource_type' => $lease->resource_type,
            'resource_id' => $lease->resource_id,
            'lease_version' => $lease->lease_version,
            'current_holder_device_id' => $lease->holder_device_id,
            'lease_expires_at' => $lease->lease_expires_at?->toIso8601String(),
            'staff_profile_id' => $staffProfile->id,
            'starts_at' => $startsAtUtc->toIso8601String(),
            'ends_at' => $endsAtUtc->toIso8601String(),
        ];
    }

    public function resourceId(
        string $staffProfileId,
        CarbonImmutable $startsAtUtc,
        CarbonImmutable $endsAtUtc,
    ): string {
        return implode(':', [
            'staff',
            $staffProfileId,
            $startsAtUtc->format('YmdHis'),
            $endsAtUtc->format('YmdHis'),
        ]);
    }
}
