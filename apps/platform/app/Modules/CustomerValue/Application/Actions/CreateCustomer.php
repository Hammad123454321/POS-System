<?php

namespace App\Modules\CustomerValue\Application\Actions;

use App\Models\User;
use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\CustomerValue\Domain\Models\MemberAccount;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class CreateCustomer
{
    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $name,
        ?string $phone,
        ?string $email,
        ?string $memberNumber,
    ): array {
        if (! $this->canManageStoreConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage customers for this store.');
        }

        return DB::transaction(function () use ($store, $name, $phone, $email, $memberNumber): array {
            $customer = Customer::query()->create([
                'merchant_id' => $store->merchant_id,
                'name' => $name,
                'phone' => $phone,
                'email' => $email,
                'is_active' => true,
            ]);

            $memberAccount = null;

            if ($memberNumber !== null && $memberNumber !== '') {
                $memberAccount = MemberAccount::query()->create([
                    'merchant_id' => $store->merchant_id,
                    'customer_id' => $customer->id,
                    'member_number' => $memberNumber,
                    'status' => 'active',
                ]);
            }

            return [
                'id' => $customer->id,
                'name' => $customer->name,
                'phone' => $customer->phone,
                'email' => $customer->email,
                'member_account' => $memberAccount === null ? null : [
                    'id' => $memberAccount->id,
                    'member_number' => $memberAccount->member_number,
                    'status' => $memberAccount->status,
                ],
            ];
        });
    }

    private function canManageStoreConfig(User $actor, Store $store): bool
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
