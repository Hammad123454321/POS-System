<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use Database\Factories\UserFactory;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Illuminate\Support\Collection;
use Illuminate\Support\Facades\DB;
use Laravel\Fortify\TwoFactorAuthenticatable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    /** @use HasFactory<UserFactory> */
    use HasApiTokens, HasFactory, Notifiable, TwoFactorAuthenticatable;

    protected $fillable = [
        'name',
        'email',
        'password',
        'is_super_admin',
    ];

    protected $hidden = [
        'password',
        'remember_token',
        'two_factor_secret',
        'two_factor_recovery_codes',
    ];

    protected ?Collection $storeRoleMap = null;

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'is_super_admin' => 'boolean',
            'password' => 'hashed',
            'two_factor_confirmed_at' => 'datetime',
        ];
    }

    public function roleNamesForStore(string $storeId): Collection
    {
        return $this->storeRoleRows()
            ->where('store_id', $storeId)
            ->pluck('role_name')
            ->unique()
            ->values();
    }

    public function hasRoleInStore(string|array $roles, string $storeId): bool
    {
        return $this->roleNamesForStore($storeId)
            ->intersect((array) $roles)
            ->isNotEmpty();
    }

    public function accessibleStoreIds(): Collection
    {
        return $this->storeRoleRows()
            ->pluck('store_id')
            ->unique()
            ->values();
    }

    public function accessibleMerchantIds(): Collection
    {
        return $this->storeRoleRows()
            ->pluck('merchant_id')
            ->filter()
            ->unique()
            ->values();
    }

    public function distinctPermissionKeys(): Collection
    {
        return $this->storeRoleRows()
            ->pluck('permission_key')
            ->filter()
            ->unique()
            ->values();
    }

    public function permissionKeysForStore(string $storeId): Collection
    {
        return $this->storeRoleRows()
            ->where('store_id', $storeId)
            ->pluck('permission_key')
            ->filter()
            ->unique()
            ->values();
    }

    public function hasPermissionInStore(string $permission, string $storeId): bool
    {
        return $this->permissionKeysForStore($storeId)->contains($permission);
    }

    public function permissionKeysForMerchant(string $merchantId): Collection
    {
        return $this->storeRoleRows()
            ->where('merchant_id', $merchantId)
            ->pluck('permission_key')
            ->filter()
            ->unique()
            ->values();
    }

    public function hasPermissionInMerchant(string $permission, string $merchantId): bool
    {
        return $this->permissionKeysForMerchant($merchantId)->contains($permission);
    }

    protected function storeRoleRows(): Collection
    {
        if ($this->storeRoleMap !== null) {
            return $this->storeRoleMap;
        }

        $this->storeRoleMap = $this->withoutRowSecurity(fn () => DB::table('user_store_role')
            ->join('stores', 'stores.id', '=', 'user_store_role.store_id')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->leftJoin('permission_role', 'permission_role.role_id', '=', 'roles.id')
            ->leftJoin('permissions', 'permissions.id', '=', 'permission_role.permission_id')
            ->where('user_store_role.user_id', $this->getAuthIdentifier())
            ->get([
                'user_store_role.store_id',
                'stores.merchant_id',
                'roles.name as role_name',
                'permissions.key as permission_key',
            ]));

        return $this->storeRoleMap;
    }

    /**
     * @template TValue
     *
     * @param  callable(): TValue  $callback
     * @return TValue
     */
    private function withoutRowSecurity(callable $callback): mixed
    {
        if (DB::connection()->getDriverName() !== 'pgsql') {
            return $callback();
        }

        DB::statement('set row_security = off');

        try {
            return $callback();
        } finally {
            DB::statement('set row_security = on');
        }
    }
}
