<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use DomainException;

class CompleteAppointment
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
        private readonly RecordUsage $recordUsage,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, Appointment $appointment): array
    {
        if ($appointment->merchant_id !== $device->merchant_id || $appointment->store_id !== $device->store_id) {
            throw new DomainException('Appointment does not belong to this store.');
        }

        if (! in_array($appointment->status, ['confirmed', 'checked_in'], true)) {
            throw new DomainException('Only confirmed or checked-in appointments can be completed.');
        }

        $now = now('UTC');

        $appointment->forceFill([
            'status' => 'completed',
            'status_seq' => $appointment->status_seq + 1,
            'checked_in_at' => $appointment->checked_in_at ?? $now,
            'completed_at' => $now,
            'updated_by_device_id' => $device->id,
        ])->save();

        $this->auditLogger->log(
            $device->merchant_id,
            $device->store_id,
            'salon_workforce',
            'appointment.completed',
            'appointment',
            $appointment->id,
            null,
            [
                'status' => $appointment->status,
                'status_seq' => $appointment->status_seq,
            ],
            null,
            null,
            $device->id,
        );
        $this->recordUsage->handle($device->merchant_id, $device->store_id, 'salon.appointment.completed');

        return [
            'id' => $appointment->id,
            'status' => $appointment->status,
            'status_seq' => $appointment->status_seq,
            'checked_in_at' => $appointment->checked_in_at?->toIso8601String(),
            'completed_at' => $appointment->completed_at?->toIso8601String(),
        ];
    }
}
