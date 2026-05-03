<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Models\User;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Support\WorkforceAuthorization;
use App\Modules\SalonWorkforce\Domain\Models\CommissionRule;
use App\Modules\SalonWorkforce\Domain\Models\ServiceItem;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use App\Modules\SalonWorkforce\Domain\Models\StaffServiceRule;
use Illuminate\Auth\Access\AuthorizationException;

class UpsertStaffServiceRule
{
    public function __construct(
        private readonly WorkforceAuthorization $authorization,
        private readonly AuditLogger $auditLogger,
        private readonly RecordUsage $recordUsage,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $staffProfileId,
        string $serviceItemId,
        ?string $commissionRuleId,
        bool $isActive,
    ): array {
        if (! $this->authorization->canManageStore($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage staff-service assignments for this store.');
        }

        $staffProfile = StaffProfile::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereKey($staffProfileId)
            ->firstOrFail();

        $serviceItem = ServiceItem::query()
            ->where('merchant_id', $store->merchant_id)
            ->whereKey($serviceItemId)
            ->firstOrFail();

        $commissionRule = $commissionRuleId === null ? null : CommissionRule::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereKey($commissionRuleId)
            ->firstOrFail();

        $assignment = StaffServiceRule::query()->updateOrCreate(
            [
                'merchant_id' => $store->merchant_id,
                'store_id' => $store->id,
                'staff_profile_id' => $staffProfile->id,
                'service_item_id' => $serviceItem->id,
            ],
            [
                'commission_rule_id' => $commissionRule?->id,
                'is_active' => $isActive,
            ],
        );

        $this->auditLogger->log(
            $store->merchant_id,
            $store->id,
            'salon_workforce',
            'staff_service_rule.upserted',
            'staff_service_rule',
            $assignment->id,
            null,
            [
                'staff_profile_id' => $assignment->staff_profile_id,
                'service_item_id' => $assignment->service_item_id,
                'commission_rule_id' => $assignment->commission_rule_id,
                'is_active' => $assignment->is_active,
            ],
            null,
            $actor->id,
        );
        $this->recordUsage->handle($store->merchant_id, $store->id, 'salon.staff_service_rule.upserted');

        return [
            'id' => $assignment->id,
            'staff_profile_id' => $assignment->staff_profile_id,
            'service_item_id' => $assignment->service_item_id,
            'commission_rule_id' => $assignment->commission_rule_id,
            'is_active' => $assignment->is_active,
        ];
    }
}
