<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\AttendanceRecord;
use App\Modules\SalonWorkforce\Domain\Models\Shift;
use DomainException;
use Illuminate\Support\Facades\DB;

class CloseShift
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        Device $device,
        Shift $shift,
        int $sessionVersion,
        int $breakMinutes,
    ): array {
        if ($shift->merchant_id !== $device->merchant_id || $shift->store_id !== $device->store_id) {
            throw new DomainException('Shift does not belong to this store.');
        }

        if ($shift->status !== 'open') {
            throw new DomainException('Only open shifts can be closed.');
        }

        if ($shift->session_version !== $sessionVersion) {
            throw new DomainException('Shift version conflict. Refresh and retry.');
        }

        return DB::transaction(function () use ($breakMinutes, $device, $shift): array {
            $closedAt = now('UTC');
            $workedMinutes = max(0, $shift->started_at?->diffInMinutes($closedAt) ?? 0);
            $netWorkedMinutes = max(0, $workedMinutes - max(0, $breakMinutes));

            $shift->forceFill([
                'status' => 'closed',
                'session_version' => $shift->session_version + 1,
                'closed_by_device_id' => $device->id,
                'ended_at' => $closedAt,
                'total_minutes' => $netWorkedMinutes,
                'metadata' => array_merge($shift->metadata ?? [], [
                    'break_minutes' => max(0, $breakMinutes),
                ]),
            ])->save();

            $attendance = AttendanceRecord::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('shift_id', $shift->id)
                ->latest('created_at')
                ->first();

            if ($attendance !== null) {
                $attendance->forceFill([
                    'check_out_at' => $closedAt,
                    'worked_minutes' => $netWorkedMinutes,
                    'break_minutes' => max(0, $breakMinutes),
                    'approval_status' => 'pending',
                ])->save();
            }

            $this->auditLogger->log(
                $device->merchant_id,
                $device->store_id,
                'salon_workforce',
                'shift.closed',
                'shift',
                $shift->id,
                null,
                [
                    'attendance_record_id' => $attendance?->id,
                    'worked_minutes' => $netWorkedMinutes,
                    'break_minutes' => max(0, $breakMinutes),
                ],
                null,
                null,
                $device->id,
            );

            return [
                'id' => $shift->id,
                'status' => $shift->status,
                'session_version' => $shift->session_version,
                'ended_at' => $shift->ended_at?->toIso8601String(),
                'total_minutes' => $shift->total_minutes,
                'attendance_record_id' => $attendance?->id,
            ];
        });
    }
}
