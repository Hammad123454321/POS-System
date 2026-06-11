<?php

use App\Models\User;
use App\Modules\Identity\Application\ProvisionMerchantRoles;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

function onboardMerchant(string $code = 'M1'): Merchant
{
    $merchant = Merchant::query()->create([
        'name' => 'Merchant '.$code,
        'currency' => 'USD',
        'status' => 'active',
    ]);
    app(ProvisionMerchantRoles::class)->handle($merchant);

    return $merchant;
}

function ownerFor(Merchant $merchant): User
{
    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Seed Store',
        'code' => 'SEED',
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    $user = User::factory()->create();
    $roleId = DB::table('roles')->where('merchant_id', $merchant->id)->where('name', 'Merchant Owner')->value('id');
    DB::table('user_store_role')->insert([
        'user_id' => $user->id,
        'store_id' => $store->id,
        'role_id' => $roleId,
        'created_at' => now(),
        'updated_at' => now(),
    ]);

    return $user;
}

it('super admin creates a merchant which auto-provisions four roles', function () {
    $superAdmin = User::factory()->create(['is_super_admin' => true]);
    Sanctum::actingAs($superAdmin);

    $response = $this->postJson('/api/super-admin/v1/merchants', [
        'name' => 'Fresh Co',
        'currency' => 'usd',
    ])->assertCreated();

    $merchantId = $response->json('data.id');

    expect(DB::table('roles')->where('merchant_id', $merchantId)->count())->toBe(4);
    expect(DB::table('roles')->where('merchant_id', $merchantId)->where('name', 'Merchant Owner')->exists())->toBeTrue();
});

it('lets a merchant owner create a store for their own merchant', function () {
    $merchant = onboardMerchant();
    $owner = ownerFor($merchant);
    Sanctum::actingAs($owner);

    $this->postJson("/api/admin/v1/merchants/{$merchant->id}/stores", [
        'name' => 'Downtown',
        'code' => 'DT',
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
    ])->assertCreated()
        ->assertJsonPath('data.name', 'Downtown');

    expect(Store::query()->where('merchant_id', $merchant->id)->where('code', 'DT')->exists())->toBeTrue();
});

it('forbids creating a store for another merchant', function () {
    $merchantA = onboardMerchant('A');
    $merchantB = onboardMerchant('B');
    $ownerA = ownerFor($merchantA);
    Sanctum::actingAs($ownerA);

    $this->postJson("/api/admin/v1/merchants/{$merchantB->id}/stores", [
        'name' => 'Sneaky',
        'mode' => 'retail',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
    ])->assertForbidden();
});

it('super admin creates a global device profile', function () {
    $superAdmin = User::factory()->create(['is_super_admin' => true]);
    Sanctum::actingAs($superAdmin);

    $this->postJson('/api/super-admin/v1/device-profiles', [
        'name' => 'Kiosk',
        'type' => 'kiosk',
        'capabilities' => ['card' => true],
    ])->assertCreated()
        ->assertJsonPath('data.name', 'Kiosk');

    expect(DeviceProfile::query()->where('name', 'Kiosk')->exists())->toBeTrue();
});

it('lists and deactivates devices for a store with devices.manage', function () {
    $merchant = onboardMerchant();
    $owner = ownerFor($merchant);

    $store = Store::query()->where('merchant_id', $merchant->id)->first();
    $profile = DeviceProfile::query()->create(['name' => 'Tablet', 'type' => 'tablet', 'capabilities' => []]);
    $device = Device::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_profile_id' => $profile->id,
        'name' => 'Reg 1',
        'platform' => 'android',
        'status' => 'active',
    ]);

    Sanctum::actingAs($owner);

    $this->getJson("/api/admin/v1/stores/{$store->id}/devices")
        ->assertOk()
        ->assertJsonPath('data.0.name', 'Reg 1');

    $this->postJson("/api/admin/v1/stores/{$store->id}/devices/{$device->id}/deactivate")
        ->assertOk()
        ->assertJsonPath('data.status', 'disabled');

    expect($device->fresh()->status)->toBe('disabled');
});

it('forbids non-super-admin from creating device profiles', function () {
    $merchant = onboardMerchant();
    $owner = ownerFor($merchant);
    Sanctum::actingAs($owner);

    $this->postJson('/api/super-admin/v1/device-profiles', [
        'name' => 'X',
        'type' => 'x',
    ])->assertForbidden();
});
