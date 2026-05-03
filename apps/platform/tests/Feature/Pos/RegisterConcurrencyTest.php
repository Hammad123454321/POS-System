<?php

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Laravel\Sanctum\Sanctum;

it('opens an exception when a second device tries to open the same drawer', function () {
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

    $deviceA = Device::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_profile_id' => $profile->id,
        'name' => 'Front Register',
        'platform' => 'windows',
        'status' => 'active',
        'drawer_code' => 'drawer-1',
    ]);

    $deviceB = Device::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_profile_id' => $profile->id,
        'name' => 'Backup Register',
        'platform' => 'windows',
        'status' => 'active',
        'drawer_code' => 'drawer-1',
    ]);

    Sanctum::actingAs($deviceA, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'register-open-1',
    ]))->postJson('/api/pos/v1/register-sessions/open', [
        'opening_float_minor' => 10000,
    ])->assertCreated();

    Sanctum::actingAs($deviceB, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'register-open-2',
    ]))->postJson('/api/pos/v1/register-sessions/open', [
        'opening_float_minor' => 10000,
    ])->assertStatus(422);

    expect(ExceptionCase::query()->where('code', 'register_session.concurrent_open')->count())->toBe(1);
});

it('blocks non-owner close attempts and opens variance exceptions when thresholds are exceeded', function () {
    [$ownerDevice, $registerSession, $item] = buildPosOrderContext();

    $otherDevice = Device::query()->create([
        'merchant_id' => $ownerDevice->merchant_id,
        'store_id' => $ownerDevice->store_id,
        'device_profile_id' => $ownerDevice->device_profile_id,
        'name' => 'Floor Device',
        'platform' => 'android',
        'status' => 'active',
        'drawer_code' => 'drawer-2',
    ]);

    Sanctum::actingAs($otherDevice, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'register-close-1',
    ]))->postJson("/api/pos/v1/register-sessions/{$registerSession->id}/close", [
        'counted_cash_minor' => 10000,
        'session_version' => 1,
    ])->assertStatus(422);

    expect(ExceptionCase::query()->where('code', 'register_session.non_owner_close')->count())->toBe(1);

    Sanctum::actingAs($ownerDevice, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'register-close-2',
    ]))->postJson("/api/pos/v1/register-sessions/{$registerSession->id}/close", [
        'counted_cash_minor' => 13050,
        'session_version' => 1,
    ])->assertOk()
        ->assertJsonPath('data.session_version', 2);

    expect(ExceptionCase::query()->where('code', 'register_session.close_variance')->count())->toBe(1);
});
