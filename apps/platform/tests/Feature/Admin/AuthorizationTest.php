<?php

use App\Models\User;
use App\Modules\Identity\Application\ProvisionMerchantRoles;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

function makeMerchantWithStore(string $code = 'STR-A'): array
{
    $merchant = Merchant::query()->create([
        'name' => 'Merchant '.$code,
        'currency' => 'USD',
        'status' => 'active',
    ]);

    app(ProvisionMerchantRoles::class)->handle($merchant);

    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Store '.$code,
        'code' => $code,
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    return [$merchant, $store];
}

function assignRole(User $user, Store $store, string $roleName): void
{
    $roleId = DB::table('roles')
        ->where('merchant_id', $store->merchant_id)
        ->where('name', $roleName)
        ->value('id');

    DB::table('user_store_role')->insert([
        'user_id' => $user->id,
        'store_id' => $store->id,
        'role_id' => $roleId,
        'created_at' => now(),
        'updated_at' => now(),
    ]);
}

it('rejects a user with no store roles on admin endpoints', function () {
    [$merchant, $store] = makeMerchantWithStore();
    $user = User::factory()->create();

    Sanctum::actingAs($user);

    $this->postJson("/api/admin/v1/stores/{$store->id}/dining-tables", [
        'name' => 'T1',
        'capacity' => 4,
    ])->assertForbidden();
});

it('blocks a store admin from acting on another merchant store', function () {
    [$merchantA, $storeA] = makeMerchantWithStore('STR-A');
    [$merchantB, $storeB] = makeMerchantWithStore('STR-B');

    $user = User::factory()->create();
    assignRole($user, $storeA, 'Store Admin');

    Sanctum::actingAs($user);

    // Allowed on own store.
    $this->postJson("/api/admin/v1/stores/{$storeA->id}/dining-tables", [
        'name' => 'A1',
        'capacity' => 2,
    ])->assertCreated();

    // Forbidden on the other merchant's store.
    $this->postJson("/api/admin/v1/stores/{$storeB->id}/dining-tables", [
        'name' => 'B1',
        'capacity' => 2,
    ])->assertForbidden();
});

it('does not leak a permission from one store into another', function () {
    [$merchant, $storeA] = makeMerchantWithStore('STR-A');

    $storeB = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Store B',
        'code' => 'STR-B',
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    $user = User::factory()->create();
    // Cashier only has orders.view; assign in store A only.
    assignRole($user, $storeA, 'Cashier');

    Sanctum::actingAs($user);

    // Cashier lacks catalog.manage even in store A.
    $this->postJson("/api/admin/v1/stores/{$storeA->id}/dining-tables", [
        'name' => 'A1',
        'capacity' => 2,
    ])->assertForbidden();

    // And has no access at all to store B.
    $this->postJson("/api/admin/v1/stores/{$storeB->id}/dining-tables", [
        'name' => 'B1',
        'capacity' => 2,
    ])->assertForbidden();
});

it('lets a super admin pass on any store', function () {
    [$merchant, $store] = makeMerchantWithStore();
    $user = User::factory()->create(['is_super_admin' => true]);

    Sanctum::actingAs($user);

    $this->postJson("/api/admin/v1/stores/{$store->id}/dining-tables", [
        'name' => 'T1',
        'capacity' => 4,
    ])->assertCreated();
});

it('rejects non-super-admins on super-admin endpoints', function () {
    $user = User::factory()->create();
    Sanctum::actingAs($user);

    $this->getJson('/api/super-admin/v1/merchants')->assertForbidden();
});

it('allows a super admin to list and create merchants', function () {
    $user = User::factory()->create(['is_super_admin' => true]);
    Sanctum::actingAs($user);

    $this->postJson('/api/super-admin/v1/merchants', [
        'name' => 'New Co',
        'currency' => 'USD',
    ])->assertCreated();

    $this->getJson('/api/super-admin/v1/merchants')
        ->assertOk()
        ->assertJsonFragment(['name' => 'New Co']);
});
