<?php

namespace App\Modules\Identity\Application;

use App\Modules\PlatformCore\Domain\Models\Merchant;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class ProvisionMerchantRoles
{
    public const PERMISSIONS = [
        'devices.manage' => 'Manage POS device enrollment and lifecycle',
        'catalog.manage' => 'Manage catalog items, categories, modifiers, and pricing',
        'orders.view' => 'View orders and order details',
        'orders.refund' => 'Refund and void order payments',
        'users.manage' => 'Invite users and manage store roles',
        'reports.view' => 'View operational and financial reports',
        'delivery.manage' => 'Manage delivery channels and availability',
        'billing.view' => 'View billing and usage information',
        'stores.manage' => 'Manage stores and onboarding settings',
    ];

    public const ROLE_PERMISSIONS = [
        'Merchant Owner' => [
            'devices.manage',
            'catalog.manage',
            'orders.view',
            'orders.refund',
            'users.manage',
            'reports.view',
            'delivery.manage',
            'billing.view',
            'stores.manage',
        ],
        'Store Admin' => [
            'devices.manage',
            'catalog.manage',
            'orders.view',
            'orders.refund',
            'users.manage',
            'reports.view',
            'delivery.manage',
            'stores.manage',
        ],
        'Store Manager' => [
            'catalog.manage',
            'orders.view',
            'orders.refund',
            'reports.view',
            'delivery.manage',
        ],
        'Cashier' => [
            'orders.view',
        ],
    ];

    public function handle(Merchant $merchant): void
    {
        $permissionIds = $this->ensurePermissions();

        foreach (self::ROLE_PERMISSIONS as $roleName => $permissionKeys) {
            $role = DB::table('roles')
                ->where('merchant_id', $merchant->id)
                ->where('name', $roleName)
                ->first();

            $roleId = $role?->id ?? (string) Str::ulid();

            DB::table('roles')->updateOrInsert(
                ['merchant_id' => $merchant->id, 'name' => $roleName],
                [
                    'id' => $roleId,
                    'scope' => 'store',
                    'updated_at' => now(),
                    'created_at' => $role?->created_at ?? now(),
                ],
            );

            foreach ($permissionKeys as $permissionKey) {
                DB::table('permission_role')->updateOrInsert([
                    'role_id' => $roleId,
                    'permission_id' => $permissionIds[$permissionKey],
                ]);
            }
        }
    }

    /**
     * @return array<string, string>
     */
    public function ensurePermissions(): array
    {
        $ids = [];

        foreach (self::PERMISSIONS as $key => $description) {
            $permission = DB::table('permissions')->where('key', $key)->first();
            $permissionId = $permission?->id ?? (string) Str::ulid();

            DB::table('permissions')->updateOrInsert(
                ['key' => $key],
                [
                    'id' => $permissionId,
                    'description' => $description,
                    'updated_at' => now(),
                    'created_at' => $permission?->created_at ?? now(),
                ],
            );

            $ids[$key] = $permissionId;
        }

        return $ids;
    }
}
