<?php

namespace App\Modules\Restaurant\Application\Actions;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Restaurant\Domain\Models\PrinterConfig;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class CreatePrinterConfig
{
    /**
     * @param  array<string, mixed>|null  $connectionConfig
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $name,
        string $driverKey,
        ?array $connectionConfig,
    ): array {
        if (! $this->canManageRestaurantConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage printer configs for this store.');
        }

        $printer = PrinterConfig::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'name' => $name,
            'driver_key' => $driverKey,
            'connection_config' => $connectionConfig,
            'is_active' => true,
        ]);

        return [
            'id' => $printer->id,
            'name' => $printer->name,
            'driver_key' => $printer->driver_key,
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
