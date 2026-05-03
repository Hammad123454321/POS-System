<?php

use App\Models\User;
use App\Modules\Identity\Domain\Models\Role;
use App\Modules\PlatformCore\Domain\Models\FeatureFlag;
use App\Modules\PlatformCore\Domain\Models\FeatureFlagOverride;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

it('upserts merchant and store feature flags for merchant owners and exposes them in POS payloads', function () {
    config()->set('pos.feature_flags.definitions', [
        'phase2-split-tender' => [
            'default' => false,
            'self_service' => true,
        ],
    ]);

    $user = User::factory()->create();

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

    $role = Role::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Merchant Owner',
        'scope' => 'store',
    ]);

    DB::table('user_store_role')->insert([
        'user_id' => $user->id,
        'store_id' => $store->id,
        'role_id' => $role->id,
        'created_at' => now(),
        'updated_at' => now(),
    ]);

    Sanctum::actingAs($user);

    $this->postJson("/api/admin/v1/stores/{$store->id}/feature-flags/phase2-split-tender", [
        'scope' => 'merchant',
        'enabled' => true,
    ])->assertOk()
        ->assertJsonPath('data.flag_key', 'phase2-split-tender')
        ->assertJsonPath('data.enabled', true)
        ->assertJsonPath('data.value', true);

    $this->postJson("/api/admin/v1/stores/{$store->id}/feature-flags/phase2-split-tender", [
        'scope' => 'store',
        'enabled' => true,
        'value' => ['mode' => 'strict'],
    ])->assertOk()
        ->assertJsonPath('data.scope', 'store')
        ->assertJsonPath('data.value.mode', 'strict');

    expect(FeatureFlag::query()->where('merchant_id', $merchant->id)->count())->toBe(1);
    expect(FeatureFlagOverride::query()->where('store_id', $store->id)->count())->toBe(1);

    [$device] = buildPosOrderContext();
    $device->forceFill([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
    ])->save();

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/bootstrap')
        ->assertOk()
        ->assertJsonPath('feature_flags.phase2-split-tender.mode', 'strict');

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/config')
        ->assertOk()
        ->assertJsonPath('feature_flags.phase2-split-tender.mode', 'strict');
});
