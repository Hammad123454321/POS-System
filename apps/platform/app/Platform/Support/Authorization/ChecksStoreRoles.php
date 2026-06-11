<?php

namespace App\Platform\Support\Authorization;

use App\Models\User;
use Illuminate\Support\Facades\DB;

/**
 * Shared helper for the legacy inline role-name checks scattered across module
 * actions. Centralizes the super-admin bypass so platform operators can act on
 * any store, and keeps the role-name query in one place.
 */
trait ChecksStoreRoles
{
    /**
     * @param  array<int, string>  $roleNames
     */
    protected function userHasStoreRole(User $actor, string $storeId, array $roleNames): bool
    {
        if ($actor->is_super_admin) {
            return true;
        }

        return DB::table('user_store_role')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->where('user_store_role.user_id', $actor->id)
            ->where('user_store_role.store_id', $storeId)
            ->whereIn('roles.name', $roleNames)
            ->exists();
    }
}
