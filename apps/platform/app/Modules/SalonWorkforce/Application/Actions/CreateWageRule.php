<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Models\User;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Support\WorkforceAuthorization;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use App\Modules\SalonWorkforce\Domain\Models\WageRule;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Carbon;

class CreateWageRule
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
        string $staffProfileId,
        string $name,
        int $hourlyRateMinor,
        string $currency,
        string $effectiveFrom,
        ?string $effectiveTo,
        bool $isActive,
        ?array $metadata = null,
    ): array {
        if (! $this->authorization->canManageStore($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage wage rules for this store.');
        }

        $staffProfile = StaffProfile::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereKey($staffProfileId)
            ->firstOrFail();

        $rule = WageRule::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'staff_profile_id' => $staffProfile->id,
            'name' => $name,
            'wage_type' => 'hourly',
            'hourly_rate_minor' => $hourlyRateMinor,
            'currency' => strtoupper($currency),
            'effective_from' => $effectiveFrom,
            'effective_to' => $effectiveTo,
            'is_active' => $isActive,
            'metadata' => $metadata,
        ]);

        $this->auditLogger->log(
            $store->merchant_id,
            $store->id,
            'salon_workforce',
            'wage_rule.created',
            'wage_rule',
            $rule->id,
            null,
            [
                'staff_profile_id' => $staffProfile->id,
                'hourly_rate_minor' => $rule->hourly_rate_minor,
                'effective_from' => $rule->effective_from?->format('Y-m-d'),
            ],
            null,
            $actor->id,
        );
        $this->recordUsage->handle($store->merchant_id, $store->id, 'salon.wage_rule.created');

        return [
            'id' => $rule->id,
            'staff_profile_id' => $rule->staff_profile_id,
            'name' => $rule->name,
            'wage_type' => $rule->wage_type,
            'hourly_rate_minor' => $rule->hourly_rate_minor,
            'currency' => $rule->currency,
            'effective_from' => $rule->effective_from?->format('Y-m-d'),
            'effective_to' => $rule->effective_to?->format('Y-m-d'),
            'is_active' => $rule->is_active,
            'created_at' => Carbon::parse($rule->created_at)->toIso8601String(),
        ];
    }
}
