<?php

namespace App\Modules\Catalog\Application\Actions;

use App\Models\User;
use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class CreateDiscountRule
{
    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $name,
        ?string $code,
        string $type,
        ?int $valueMinor,
        ?int $valueBasisPoints,
        int $sortOrder,
        string $scopeMode = 'all',
        ?array $applicability = null,
        bool $isStackable = false,
        ?string $startsAt = null,
        ?string $endsAt = null,
    ): array {
        if (! $this->canManageStoreConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage discounts for this store.');
        }

        $rule = DiscountRule::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'name' => $name,
            'code' => $code,
            'type' => $type,
            'value_minor' => $valueMinor,
            'value_basis_points' => $valueBasisPoints,
            'sort_order' => $sortOrder,
            'scope_mode' => $scopeMode,
            'applicability' => $applicability,
            'is_stackable' => $isStackable,
            'starts_at' => $startsAt,
            'ends_at' => $endsAt,
            'is_active' => true,
        ]);

        return [
            'id' => $rule->id,
            'name' => $rule->name,
            'code' => $rule->code,
            'type' => $rule->type,
            'value_minor' => $rule->value_minor,
            'value_basis_points' => $rule->value_basis_points,
            'scope_mode' => $rule->scope_mode,
            'applicability' => $rule->applicability,
            'is_stackable' => $rule->is_stackable,
            'starts_at' => $rule->starts_at?->toIso8601String(),
            'ends_at' => $rule->ends_at?->toIso8601String(),
        ];
    }

    private function canManageStoreConfig(User $actor, Store $store): bool
    {
        return DB::table('user_store_role')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->where('user_store_role.user_id', $actor->id)
            ->where('user_store_role.store_id', $store->id)
            ->whereIn('roles.name', ['Merchant Owner', 'Store Admin'])
            ->exists();
    }
}
