<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Models\User;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Support\WorkforceAuthorization;
use App\Modules\SalonWorkforce\Domain\Models\AttendanceRecord;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class ApproveAttendanceRecord
{
    public function __construct(
        private readonly WorkforceAuthorization $authorization,
        private readonly AuditLogger $auditLogger,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        AttendanceRecord $record,
    ): array {
        if (! $this->authorization->canManageStore($actor, $store)) {
            throw new AuthorizationException('You are not allowed to approve attendance for this store.');
        }

        if ($record->merchant_id !== $store->merchant_id || $record->store_id !== $store->id) {
            throw new AuthorizationException('The attendance record does not belong to this store.');
        }

        DB::transaction(function () use ($actor, $record, $store): void {
            $record->forceFill([
                'approval_status' => 'approved',
                'approved_by_user_id' => $actor->id,
                'approved_at' => now('UTC'),
            ])->save();

            $this->auditLogger->log(
                $store->merchant_id,
                $store->id,
                'salon_workforce',
                'attendance_record.approved',
                'attendance_record',
                $record->id,
                null,
                [
                    'worked_minutes' => $record->worked_minutes,
                    'approved_by_user_id' => $actor->id,
                ],
                null,
                $actor->id,
            );
        });

        return [
            'id' => $record->id,
            'approval_status' => $record->approval_status,
            'approved_by_user_id' => $record->approved_by_user_id,
            'approved_at' => $record->approved_at?->toIso8601String(),
            'worked_minutes' => $record->worked_minutes,
        ];
    }
}
