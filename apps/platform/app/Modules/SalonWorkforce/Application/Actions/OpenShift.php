<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\AttendanceRecord;
use App\Modules\SalonWorkforce\Domain\Models\Shift;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use App\Platform\Support\Time\BusinessClock;
use DomainException;
use Illuminate\Support\Facades\DB;

class OpenShift
{
    public function __construct(
        private readonly BusinessClock $businessClock,
        private readonly OpenExceptionCase $openExceptionCase,
        private readonly AuditLogger $auditLogger,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        Device $device,
        string $staffProfileId,
        ?string $scheduledStartAt,
        ?string $scheduledEndAt,
    ): array {
        $staffProfile = StaffProfile::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('status', 'active')
            ->whereKey($staffProfileId)
            ->firstOrFail();

        $existingOpenShift = Shift::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('staff_profile_id', $staffProfile->id)
            ->where('status', 'open')
            ->first();

        if ($existingOpenShift !== null) {
            $this->openExceptionCase->handle(
                $device->merchant_id,
                $device->store_id,
                'workforce',
                'SHIFT_CONCURRENT_OPEN',
                'high',
                'Concurrent shift open attempt rejected for staff member.',
                [
                    'staff_profile_id' => $staffProfile->id,
                    'existing_shift_id' => $existingOpenShift->id,
                ],
                'shift',
                $existingOpenShift->id,
                $device->id,
            );

            throw new DomainException('This staff member already has an open shift.');
        }

        return DB::transaction(function () use ($device, $scheduledEndAt, $scheduledStartAt, $staffProfile): array {
            $now = now('UTC');
            $store = $device->store()->firstOrFail();

            $shift = Shift::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'staff_profile_id' => $staffProfile->id,
                'opened_by_device_id' => $device->id,
                'business_date' => $this->businessClock->businessDateForStore($store),
                'status' => 'open',
                'session_version' => 1,
                'scheduled_start_at' => $scheduledStartAt,
                'scheduled_end_at' => $scheduledEndAt,
                'started_at' => $now,
            ]);

            $attendance = AttendanceRecord::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'staff_profile_id' => $staffProfile->id,
                'shift_id' => $shift->id,
                'check_in_at' => $now,
                'approval_status' => 'pending',
                'business_date' => $shift->business_date,
            ]);

            $this->auditLogger->log(
                $device->merchant_id,
                $device->store_id,
                'salon_workforce',
                'shift.opened',
                'shift',
                $shift->id,
                null,
                [
                    'staff_profile_id' => $staffProfile->id,
                    'attendance_record_id' => $attendance->id,
                ],
                null,
                null,
                $device->id,
            );

            return [
                'id' => $shift->id,
                'staff_profile_id' => $shift->staff_profile_id,
                'status' => $shift->status,
                'session_version' => $shift->session_version,
                'business_date' => $shift->business_date?->format('Y-m-d'),
                'started_at' => $shift->started_at?->toIso8601String(),
                'attendance_record_id' => $attendance->id,
            ];
        });
    }
}
