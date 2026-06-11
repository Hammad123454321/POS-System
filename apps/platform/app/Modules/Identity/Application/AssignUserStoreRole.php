<?php

namespace App\Modules\Identity\Application;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;

class AssignUserStoreRole
{
    public function handle(User $user, Store $store, string $roleId): void
    {
        $roleExists = DB::table('roles')
            ->where('id', $roleId)
            ->where('merchant_id', $store->merchant_id)
            ->exists();

        if (! $roleExists) {
            throw new \InvalidArgumentException('Role does not belong to this store\'s merchant.');
        }

        DB::table('user_store_role')->updateOrInsert(
            [
                'user_id' => $user->id,
                'store_id' => $store->id,
                'role_id' => $roleId,
            ],
            ['updated_at' => now(), 'created_at' => now()],
        );
    }
}
