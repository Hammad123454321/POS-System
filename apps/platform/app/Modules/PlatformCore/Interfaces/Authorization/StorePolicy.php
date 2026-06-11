<?php

namespace App\Modules\PlatformCore\Interfaces\Authorization;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Authorization\ResolvesStorePermissions;

class StorePolicy
{
    use ResolvesStorePermissions;

    public function manageDevices(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'devices.manage');
    }

    public function manageCatalog(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'catalog.manage');
    }

    public function viewOrders(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'orders.view');
    }

    public function refundOrders(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'orders.refund');
    }

    public function manageUsers(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'users.manage');
    }

    public function viewReports(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'reports.view');
    }

    public function manageDelivery(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'delivery.manage');
    }

    public function viewBilling(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'billing.view');
    }

    public function manageStores(User $user, Store $store): bool
    {
        return $this->canForStore($user, $store, 'stores.manage');
    }
}
