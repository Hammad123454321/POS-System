<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Models\User;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Support\WorkforceAuthorization;
use App\Modules\SalonWorkforce\Domain\Models\CommissionRule;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Carbon;

class CreateCommissionRule
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
        string $name,
        string $baseType,
        ?int $rateBasisPoints,
        ?int $fixedMinor,
        string $currency,
        ?string $effectiveFrom,
        ?string $effectiveTo,
        bool $isActive,
    ): array {
        if (! $this->authorization->canManageStore($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage commission rules for this store.');
        }

        $rule = CommissionRule::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'name' => $name,
            'base_type' => $baseType,
            'rate_basis_points' => $rateBasisPoints,
            'fixed_minor' => $fixedMinor,
            'currency' => strtoupper($currency),
            'effective_from' => $effectiveFrom,
            'effective_to' => $effectiveTo,
            'is_active' => $isActive,
        ]);

        $this->auditLogger->log(
            $store->merchant_id,
            $store->id,
            'salon_workforce',
            'commission_rule.created',
            'commission_rule',
            $rule->id,
            null,
            [
                'base_type' => $rule->base_type,
                'rate_basis_points' => $rule->rate_basis_points,
                'fixed_minor' => $rule->fixed_minor,
            ],
            null,
            $actor->id,
        );
        $this->recordUsage->handle($store->merchant_id, $store->id, 'salon.commission_rule.created');

        return [
            'id' => $rule->id,
            'name' => $rule->name,
            'base_type' => $rule->base_type,
            'rate_basis_points' => $rule->rate_basis_points,
            'fixed_minor' => $rule->fixed_minor,
            'currency' => $rule->currency,
            'effective_from' => $rule->effective_from?->format('Y-m-d'),
            'effective_to' => $rule->effective_to?->format('Y-m-d'),
            'is_active' => $rule->is_active,
            'created_at' => Carbon::parse($rule->created_at)->toIso8601String(),
        ];
    }
}
