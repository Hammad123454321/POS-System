<?php

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\FeatureFlag;
use Laravel\Sanctum\Sanctum;

beforeEach(function () {
    config()->set('pos.feature_flags.definitions', [
        'phase4-delivery' => ['default' => false, 'self_service' => false],
    ]);
});

it('lets a super admin toggle a global flag that surfaces in POS config', function () {
    $superAdmin = User::factory()->create(['is_super_admin' => true]);
    Sanctum::actingAs($superAdmin);

    $this->postJson('/api/super-admin/v1/feature-flags/phase4-delivery', [
        'enabled' => true,
    ])->assertOk()
        ->assertJsonPath('data.scope', 'global')
        ->assertJsonPath('data.enabled', true);

    expect(FeatureFlag::query()->whereNull('merchant_id')->where('flag_key', 'phase4-delivery')->count())->toBe(1);

    // A device with no merchant-level override inherits the global flag.
    [$device] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/config')
        ->assertOk()
        ->assertJsonPath('feature_flags.phase4-delivery', true);
});

it('lists global flags with their definitions', function () {
    $superAdmin = User::factory()->create(['is_super_admin' => true]);
    Sanctum::actingAs($superAdmin);

    $this->getJson('/api/super-admin/v1/feature-flags')
        ->assertOk()
        ->assertJsonFragment(['flag_key' => 'phase4-delivery', 'enabled' => false]);
});

it('rejects an unknown flag key', function () {
    $superAdmin = User::factory()->create(['is_super_admin' => true]);
    Sanctum::actingAs($superAdmin);

    $this->postJson('/api/super-admin/v1/feature-flags/nope', ['enabled' => true])
        ->assertStatus(422);
});

it('blocks a device of a suspended merchant from bootstrapping', function () {
    [$device] = buildPosOrderContext();
    $merchantId = $device->merchant_id;

    $superAdmin = User::factory()->create(['is_super_admin' => true]);
    Sanctum::actingAs($superAdmin);

    $this->postJson("/api/super-admin/v1/merchants/{$merchantId}/suspend")
        ->assertOk();

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/bootstrap')
        ->assertForbidden();
});
