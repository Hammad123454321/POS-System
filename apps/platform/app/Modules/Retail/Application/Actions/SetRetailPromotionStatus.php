<?php

namespace App\Modules\Retail\Application\Actions;

use App\Models\User;
use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class SetRetailPromotionStatus
{
    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        DiscountRule $promotion,
        bool $isActive,
    ): array {
        if (! $this->canManageStoreConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage retail promotions for this store.');
        }

        if (
            $promotion->merchant_id !== $store->merchant_id
            || $promotion->store_id !== $store->id
            || $promotion->scope_mode !== 'retail'
        ) {
            throw new AuthorizationException('This promotion does not belong to the requested store.');
        }

        $promotion->forceFill([
            'is_active' => $isActive,
        ])->save();

        return [
            'id' => $promotion->id,
            'is_active' => $promotion->is_active,
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
