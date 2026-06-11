<?php

namespace App\Modules\Restaurant\Application\Actions;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Restaurant\Domain\Models\DiningTable;
use App\Modules\Restaurant\Domain\Models\TableAssignment;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class CreateDiningTable
{
    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $name,
        ?string $zoneName,
        int $capacity,
        int $sortOrder,
    ): array {
        if (! $this->canManageRestaurantConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage dining tables for this store.');
        }

        $table = DiningTable::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'name' => $name,
            'zone_name' => $zoneName,
            'capacity' => $capacity,
            'sort_order' => $sortOrder,
            'is_active' => true,
        ]);

        TableAssignment::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'dining_table_id' => $table->id,
            'status' => 'available',
        ]);

        return [
            'id' => $table->id,
            'name' => $table->name,
            'zone_name' => $table->zone_name,
            'capacity' => $table->capacity,
            'status' => 'available',
        ];
    }

    private function canManageRestaurantConfig(User $actor, Store $store): bool
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
