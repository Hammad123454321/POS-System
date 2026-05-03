<?php

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Laravel\Sanctum\Sanctum;

it('returns the authenticated device bootstrap payload', function () {
    $merchant = Merchant::query()->create([
        'name' => 'Merchant A',
        'currency' => 'USD',
        'status' => 'active',
    ]);

    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Store A',
        'code' => 'STR-A',
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    $profile = DeviceProfile::query()->create([
        'name' => 'Counter POS',
        'type' => 'countertop',
        'capabilities' => ['receipt_printer' => true],
    ]);

    $device = Device::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_profile_id' => $profile->id,
        'name' => 'Front Register',
        'platform' => 'windows',
        'status' => 'active',
        'drawer_code' => 'drawer-1',
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $response = $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/bootstrap');

    $response->assertOk()
        ->assertJsonPath('merchant.id', $merchant->id)
        ->assertJsonPath('store.id', $store->id)
        ->assertJsonPath('device.id', $device->id)
        ->assertJsonPath('support.api_major', 1);
});
