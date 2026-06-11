<?php

namespace App\Modules\PlatformCore\Interfaces\Authorization;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Platform\Support\Authorization\ResolvesStorePermissions;

class MerchantPolicy
{
    use ResolvesStorePermissions;

    public function manageStores(User $user, Merchant $merchant): bool
    {
        return $this->canForMerchant($user, $merchant, 'stores.manage');
    }

    public function viewBilling(User $user, Merchant $merchant): bool
    {
        return $this->canForMerchant($user, $merchant, 'billing.view');
    }
}
