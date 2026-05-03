<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Application\Concurrency\EditLeaseException;
use App\Modules\PlatformCore\Application\Concurrency\ReleaseEditLease;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use App\Modules\SalonWorkforce\Domain\Models\ServiceItem;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use App\Platform\Support\Time\BusinessClock;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Database\QueryException;
use Illuminate\Support\Facades\DB;

class CreateAppointment
{
    public function __construct(
        private readonly ClaimAppointmentSlot $claimAppointmentSlot,
        private readonly ReleaseEditLease $releaseEditLease,
        private readonly BusinessClock $businessClock,
        private readonly OpenExceptionCase $openExceptionCase,
        private readonly AuditLogger $auditLogger,
        private readonly RecordUsage $recordUsage,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        Device $device,
        string $slotClaimId,
        string $staffProfileId,
        string $serviceItemId,
        ?string $customerId,
        string $startsAt,
        string $endsAt,
        string $source,
        int $discountMinor,
        ?string $notes,
    ): array {
        $store = $device->store()->firstOrFail();
        $startsAtUtc = CarbonImmutable::parse($startsAt)->setTimezone('UTC');
        $endsAtUtc = CarbonImmutable::parse($endsAt)->setTimezone('UTC');

        if ($endsAtUtc->lte($startsAtUtc)) {
            throw new DomainException('Appointment end time must be after start time.');
        }

        $staffProfile = StaffProfile::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('status', 'active')
            ->whereKey($staffProfileId)
            ->firstOrFail();

        $serviceItem = ServiceItem::query()
            ->where('merchant_id', $device->merchant_id)
            ->whereKey($serviceItemId)
            ->with('catalogItem')
            ->firstOrFail();

        /** @var EditLease $slotClaim */
        $slotClaim = EditLease::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('resource_type', 'slot_claim')
            ->whereKey($slotClaimId)
            ->firstOrFail();

        if ($slotClaim->holder_device_id !== $device->id) {
            throw new DomainException('Slot claim is not owned by this device.');
        }

        if ($slotClaim->lease_expires_at === null || $slotClaim->lease_expires_at->lte(now('UTC'))) {
            throw new DomainException('Slot claim expired before appointment confirmation.');
        }

        $expectedResourceId = $this->claimAppointmentSlot->resourceId(
            $staffProfile->id,
            $startsAtUtc,
            $endsAtUtc,
        );

        if ($slotClaim->resource_id !== $expectedResourceId) {
            throw new DomainException('Slot claim does not match this appointment time window.');
        }

        if ($this->hasOverlappingAppointment($device->merchant_id, $device->store_id, $staffProfile->id, $startsAtUtc, $endsAtUtc)) {
            $this->openOverlapException($device, $staffProfile->id, $startsAtUtc, $endsAtUtc);

            throw new DomainException('This staff member already has a confirmed appointment in the selected time range.');
        }

        try {
            return DB::transaction(function () use (
                $customerId,
                $device,
                $discountMinor,
                $endsAtUtc,
                $notes,
                $serviceItem,
                $slotClaim,
                $source,
                $staffProfile,
                $startsAtUtc,
                $store,
            ): array {
                $appointment = Appointment::query()->create([
                    'merchant_id' => $device->merchant_id,
                    'store_id' => $device->store_id,
                    'customer_id' => $customerId,
                    'staff_profile_id' => $staffProfile->id,
                    'service_item_id' => $serviceItem->id,
                    'slot_claim_id' => $slotClaim->id,
                    'status' => 'confirmed',
                    'status_seq' => 1,
                    'source' => $source,
                    'business_date' => $this->businessClock->businessDateForStore($store, $startsAtUtc),
                    'starts_at' => $startsAtUtc,
                    'ends_at' => $endsAtUtc,
                    'service_name' => $serviceItem->catalogItem?->name ?? 'Service',
                    'service_price_minor' => (int) ($serviceItem->catalogItem?->base_price_minor ?? 0),
                    'discount_minor' => max(0, $discountMinor),
                    'currency' => $serviceItem->catalogItem?->currency ?? 'USD',
                    'notes' => $notes,
                    'created_by_device_id' => $device->id,
                    'updated_by_device_id' => $device->id,
                ]);

                try {
                    $this->releaseEditLease->handle($device, 'slot_claim', $slotClaim->resource_id);
                } catch (EditLeaseException) {
                    // The appointment is already persisted; lease release can be retried independently.
                }

                $this->auditLogger->log(
                    $device->merchant_id,
                    $device->store_id,
                    'salon_workforce',
                    'appointment.created',
                    'appointment',
                    $appointment->id,
                    null,
                    [
                        'staff_profile_id' => $appointment->staff_profile_id,
                        'service_item_id' => $appointment->service_item_id,
                        'starts_at' => $appointment->starts_at?->toIso8601String(),
                        'ends_at' => $appointment->ends_at?->toIso8601String(),
                    ],
                    null,
                    null,
                    $device->id,
                );
                $this->recordUsage->handle($device->merchant_id, $device->store_id, 'salon.appointment.created');

                return $this->toPayload($appointment, $staffProfile->display_name);
            });
        } catch (QueryException $exception) {
            if (str_contains($exception->getMessage(), 'appointments_no_overlapping_staff_bookings')) {
                $this->openOverlapException($device, $staffProfile->id, $startsAtUtc, $endsAtUtc);

                throw new DomainException('This staff member already has a confirmed appointment in the selected time range.');
            }

            throw $exception;
        }
    }

    private function hasOverlappingAppointment(
        string $merchantId,
        string $storeId,
        string $staffProfileId,
        CarbonImmutable $startsAtUtc,
        CarbonImmutable $endsAtUtc,
    ): bool {
        return Appointment::query()
            ->where('merchant_id', $merchantId)
            ->where('store_id', $storeId)
            ->where('staff_profile_id', $staffProfileId)
            ->whereIn('status', ['confirmed', 'checked_in'])
            ->where('starts_at', '<', $endsAtUtc)
            ->where('ends_at', '>', $startsAtUtc)
            ->exists();
    }

    private function openOverlapException(
        Device $device,
        string $staffProfileId,
        CarbonImmutable $startsAtUtc,
        CarbonImmutable $endsAtUtc,
    ): void {
        $this->openExceptionCase->handle(
            $device->merchant_id,
            $device->store_id,
            'workforce',
            'APPOINTMENT_DOUBLE_BOOKED',
            'high',
            'Required staff double-booked for overlapping appointment window.',
            [
                'staff_profile_id' => $staffProfileId,
                'starts_at' => $startsAtUtc->toIso8601String(),
                'ends_at' => $endsAtUtc->toIso8601String(),
            ],
            'staff_profile',
            $staffProfileId,
            $device->id,
        );
    }

    /**
     * @return array<string, mixed>
     */
    private function toPayload(Appointment $appointment, string $staffDisplayName): array
    {
        return [
            'id' => $appointment->id,
            'status' => $appointment->status,
            'status_seq' => $appointment->status_seq,
            'business_date' => $appointment->business_date?->format('Y-m-d'),
            'staff_profile_id' => $appointment->staff_profile_id,
            'staff_display_name' => $staffDisplayName,
            'service_item_id' => $appointment->service_item_id,
            'service_name' => $appointment->service_name,
            'service_price_minor' => $appointment->service_price_minor,
            'discount_minor' => $appointment->discount_minor,
            'currency' => $appointment->currency,
            'starts_at' => $appointment->starts_at?->toIso8601String(),
            'ends_at' => $appointment->ends_at?->toIso8601String(),
            'source' => $appointment->source,
            'slot_claim_id' => $appointment->slot_claim_id,
        ];
    }
}
