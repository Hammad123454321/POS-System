<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Models\User;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Support\WorkforceAuthorization;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use Illuminate\Auth\Access\AuthorizationException;

class CreateStaffProfile
{
    public function __construct(
        private readonly WorkforceAuthorization $authorization,
        private readonly AuditLogger $auditLogger,
        private readonly RecordUsage $recordUsage,
    ) {}

    /**
     * @param  array<string, mixed>|null  $metadata
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $displayName,
        ?string $code,
        ?string $roleTitle,
        ?string $phone,
        ?string $email,
        ?string $hiredOn,
        ?array $metadata = null,
    ): array {
        if (! $this->authorization->canManageStore($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage staff for this store.');
        }

        $staff = StaffProfile::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'display_name' => $displayName,
            'code' => $code,
            'role_title' => $roleTitle,
            'phone' => $phone,
            'email' => $email,
            'status' => 'active',
            'hired_on' => $hiredOn,
            'metadata' => $metadata,
        ]);

        $this->auditLogger->log(
            $store->merchant_id,
            $store->id,
            'salon_workforce',
            'staff_profile.created',
            'staff_profile',
            $staff->id,
            null,
            [
                'display_name' => $staff->display_name,
                'code' => $staff->code,
                'status' => $staff->status,
            ],
            null,
            $actor->id,
        );
        $this->recordUsage->handle($store->merchant_id, $store->id, 'salon.staff_profile.created');

        return [
            'id' => $staff->id,
            'display_name' => $staff->display_name,
            'code' => $staff->code,
            'role_title' => $staff->role_title,
            'phone' => $staff->phone,
            'email' => $staff->email,
            'status' => $staff->status,
            'hired_on' => $staff->hired_on?->format('Y-m-d'),
        ];
    }
}
