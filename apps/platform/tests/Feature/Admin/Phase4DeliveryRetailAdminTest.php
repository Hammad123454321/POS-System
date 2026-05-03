<?php

use App\Models\User;
use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\Identity\Domain\Models\Role;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Domain\Models\BarcodeRecord;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

function buildPhase4AdminContext(): array
{
    $user = User::factory()->create();

    $merchant = Merchant::query()->create([
        'name' => 'Phase4 Merchant',
        'currency' => 'USD',
        'status' => 'active',
    ]);

    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Phase4 Store',
        'code' => 'PH4-1',
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    $role = Role::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Store Admin',
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

    return [$user, $merchant, $store];
}

it('manages delivery channel config and queues menu publish', function () {
    [, , $store] = buildPhase4AdminContext();

    $config = $this->postJson("/api/admin/v1/stores/{$store->id}/delivery/channel-configs", [
        'channel_key' => 'aggregator',
        'is_enabled' => true,
        'credentials' => ['api_key' => 'secret'],
        'mapping' => ['store_external_id' => 'EXT-STORE-42'],
        'default_prep_time_minutes' => 25,
        'sync_hours_enabled' => true,
        'sync_prep_time_enabled' => true,
        'sync_menu_enabled' => true,
    ])->assertCreated()
        ->assertJsonPath('data.channel_key', 'aggregator')
        ->json('data');

    $this->postJson("/api/admin/v1/stores/{$store->id}/delivery/channel-configs/{$config['id']}/publish-menu")
        ->assertStatus(202)
        ->assertJsonPath('data.queued', true);

    $this->postJson("/api/admin/v1/stores/{$store->id}/delivery/channel-configs/{$config['id']}/store-availability", [
        'state' => 'pause',
        'reason' => 'kitchen paused',
    ])->assertOk()
        ->assertJsonPath('data.channel_key', 'aggregator')
        ->assertJsonPath('data.state', 'pause');

    $this->postJson("/api/admin/v1/stores/{$store->id}/delivery/channel-configs/{$config['id']}/disconnect")
        ->assertOk()
        ->assertJsonPath('data.is_enabled', false);

    expect(DeliveryChannelConfig::query()->findOrFail($config['id'])->is_enabled)->toBeFalse();
});

it('manages barcode records and exposes phase 4 reporting endpoints', function () {
    [, , $store] = buildPhase4AdminContext();

    $this->postJson("/api/admin/v1/stores/{$store->id}/retail/barcode-records", [
        'sku' => 'SKU-123',
        'barcode' => 'BC-123',
        'is_primary' => true,
    ])->assertCreated()
        ->assertJsonPath('data.sku', 'SKU-123')
        ->assertJsonPath('data.barcode', 'BC-123');

    expect(BarcodeRecord::query()->where('store_id', $store->id)->where('barcode', 'BC-123')->exists())->toBeTrue();

    $this->getJson("/api/admin/v1/stores/{$store->id}/reports/delivery-health")
        ->assertOk()
        ->assertJsonStructure(['data' => ['received_orders_count', 'open_delivery_exceptions_count']]);

    $this->getJson("/api/admin/v1/stores/{$store->id}/reports/retail-stock-movements")
        ->assertOk()
        ->assertJsonStructure(['data' => ['movement_count', 'net_quantity_delta', 'by_adjustment_type']]);
});

it('manages retail promotions through admin create update activate and deactivate endpoints', function () {
    [, , $store] = buildPhase4AdminContext();

    $promotion = $this->postJson("/api/admin/v1/stores/{$store->id}/retail/promotions", [
        'name' => 'Retail Promo 10%',
        'code' => 'RTL10',
        'type' => 'percent_basis_points',
        'value_basis_points' => 1000,
        'priority' => 100,
        'is_stackable' => false,
        'applicability' => [
            'skus' => ['SKU-123'],
        ],
    ])->assertCreated()
        ->assertJsonPath('data.scope_mode', 'retail')
        ->assertJsonPath('data.is_active', false)
        ->json('data');

    $this->putJson("/api/admin/v1/stores/{$store->id}/retail/promotions/{$promotion['id']}", [
        'name' => 'Retail Promo 15%',
        'code' => 'RTL15',
        'type' => 'percent_basis_points',
        'value_basis_points' => 1500,
        'priority' => 200,
        'is_stackable' => false,
        'applicability' => [
            'skus' => ['SKU-123'],
        ],
    ])->assertOk()
        ->assertJsonPath('data.name', 'Retail Promo 15%')
        ->assertJsonPath('data.priority', 200);

    $this->postJson("/api/admin/v1/stores/{$store->id}/retail/promotions/{$promotion['id']}/activate")
        ->assertOk()
        ->assertJsonPath('data.is_active', true);

    $this->postJson("/api/admin/v1/stores/{$store->id}/retail/promotions/{$promotion['id']}/deactivate")
        ->assertOk()
        ->assertJsonPath('data.is_active', false);

    expect(DiscountRule::query()->findOrFail($promotion['id'])->scope_mode)->toBe('retail');
});
