<?php

namespace App\Modules\Identity\Application;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;

class RevokeUserStoreRole
{
    public function handle(User $user, Store $store, string $roleId): void
    {
        $roleName = DB::table('roles')->where('id', $roleId)->value('name');

        // Lockout protection: do not let the last Merchant Owner of a merchant
        // revoke their own owner role and orphan the merchant.
        if ($roleName === 'Merchant Owner') {
            $ownerCount = DB::table('user_store_role')
                ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
                ->join('stores', 'stores.id', '=', 'user_store_role.store_id')
                ->where('stores.merchant_id', $store->merchant_id)
                ->where('roles.name', 'Merchant Owner')
                ->distinct('user_store_role.user_id')
                ->count('user_store_role.user_id');

            if ($ownerCount <= 1) {
                throw new \DomainException('Cannot revoke the last Merchant Owner for this merchant.');
            }
        }

        DB::table('user_store_role')
            ->where('user_id', $user->id)
            ->where('store_id', $store->id)
            ->where('role_id', $roleId)
            ->delete();
    }
}
