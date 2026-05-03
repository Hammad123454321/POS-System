<?php

namespace App\Modules\Retail\Application\Actions;

use App\Models\User;
use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class UpsertRetailPromotion
{
    /**
     * @param  array<string, mixed>|null  $applicability
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
        int $priority,
        bool $isStackable,
        ?string $startsAt = null,
        ?string $endsAt = null,
        ?array $applicability = null,
        ?DiscountRule $existing = null,
    ): array {
        if (! $this->canManageStoreConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage retail promotions for this store.');
        }

        if ($existing !== null && ($existing->merchant_id !== $store->merchant_id || $existing->store_id !== $store->id)) {
            throw new AuthorizationException('This promotion does not belong to the requested store.');
        }

        $rule = $existing ?? new DiscountRule;

        $rule->forceFill([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'name' => $name,
            'code' => $code,
            'type' => $type,
            'scope_mode' => 'retail',
            'value_minor' => $valueMinor,
            'value_basis_points' => $valueBasisPoints,
            'sort_order' => $priority,
            'applicability' => $applicability,
            'is_stackable' => $isStackable,
            'starts_at' => $startsAt,
            'ends_at' => $endsAt,
            'is_active' => $existing?->is_active ?? false,
        ])->save();

        return $this->toPayload($rule->refresh());
    }

    /**
     * @return array<string, mixed>
     */
    public function toPayload(DiscountRule $rule): array
    {
        return [
            'id' => $rule->id,
            'name' => $rule->name,
            'code' => $rule->code,
            'type' => $rule->type,
            'scope_mode' => $rule->scope_mode,
            'value_minor' => $rule->value_minor,
            'value_basis_points' => $rule->value_basis_points,
            'priority' => $rule->sort_order,
            'is_stackable' => $rule->is_stackable,
            'is_active' => $rule->is_active,
            'starts_at' => $rule->starts_at?->toIso8601String(),
            'ends_at' => $rule->ends_at?->toIso8601String(),
            'applicability' => $rule->applicability,
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
