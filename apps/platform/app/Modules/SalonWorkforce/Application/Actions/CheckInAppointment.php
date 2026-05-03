<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use DomainException;

class CheckInAppointment
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, Appointment $appointment): array
    {
        if ($appointment->merchant_id !== $device->merchant_id || $appointment->store_id !== $device->store_id) {
            throw new DomainException('Appointment does not belong to this store.');
        }

        if ($appointment->status !== 'confirmed') {
            throw new DomainException('Only confirmed appointments can be checked in.');
        }

        $appointment->forceFill([
            'status' => 'checked_in',
            'status_seq' => $appointment->status_seq + 1,
            'checked_in_at' => now('UTC'),
            'updated_by_device_id' => $device->id,
        ])->save();

        $this->auditLogger->log(
            $device->merchant_id,
            $device->store_id,
            'salon_workforce',
            'appointment.checked_in',
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

        return [
            'id' => $appointment->id,
            'status' => $appointment->status,
            'status_seq' => $appointment->status_seq,
            'checked_in_at' => $appointment->checked_in_at?->toIso8601String(),
        ];
    }
}
