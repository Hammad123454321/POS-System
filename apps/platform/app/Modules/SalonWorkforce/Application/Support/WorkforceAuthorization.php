<?php

namespace App\Modules\SalonWorkforce\Application\Support;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;

class WorkforceAuthorization
{
    public function canManageStore(User $actor, Store $store): bool
    {
        if ($actor->is_super_admin) {
            return true;
        }

        return DB::table('user_store_role')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->where('user_store_role.user_id', $actor->id)
            ->where('user_store_role.store_id', $store->id)
            ->whereIn('roles.name', ['Merchant Owner', 'Store Admin'])
            ->exists();
    }
}
