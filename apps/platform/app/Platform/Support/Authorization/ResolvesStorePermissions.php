<?php

namespace App\Platform\Support\Authorization;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;

trait ResolvesStorePermissions
{
    protected function canForStore(User $user, Store $store, string $permission): bool
    {
        if ($user->is_super_admin) {
            return true;
        }

        return $user->hasPermissionInStore($permission, $store->id);
    }

    protected function canForMerchant(User $user, Merchant $merchant, string $permission): bool
    {
        if ($user->is_super_admin) {
            return true;
        }

        return $user->hasPermissionInMerchant($permission, $merchant->id);
    }
}
