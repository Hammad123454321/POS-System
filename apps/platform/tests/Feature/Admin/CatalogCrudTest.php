<?php

use App\Models\User;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Identity\Application\ProvisionMerchantRoles;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Laravel\Sanctum\Sanctum;

function catalogCtx(string $code = 'C1'): array
{
    $merchant = Merchant::query()->create(['name' => 'M '.$code, 'currency' => 'USD', 'status' => 'active']);
    app(ProvisionMerchantRoles::class)->handle($merchant);

    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'S '.$code,
        'code' => $code,
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    return [$merchant, $store];
}

function catalogAdmin(Store $store): User
{
    $user = User::factory()->create();
    $roleId = DB::table('roles')->where('merchant_id', $store->merchant_id)->where('name', 'Store Admin')->value('id');
    DB::table('user_store_role')->insert([
        'user_id' => $user->id, 'store_id' => $store->id, 'role_id' => $roleId,
        'created_at' => now(), 'updated_at' => now(),
    ]);

    return $user;
}

it('creates, updates and deactivates a catalog item', function () {
    [$merchant, $store] = catalogCtx();
    Sanctum::actingAs(catalogAdmin($store));

    $created = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/items", [
        'name' => 'Latte',
        'type' => 'product',
        'base_price_minor' => 450,
        'currency' => 'USD',
        'sku' => 'LAT-01',
    ])->assertCreated()->json('data');

    $itemId = $created['id'];

    $this->putJson("/api/admin/v1/stores/{$store->id}/catalog/items/{$itemId}", [
        'base_price_minor' => 500,
    ])->assertOk()
        ->assertJsonPath('data.base_price_minor', 500);

    $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/items/{$itemId}/deactivate")
        ->assertOk()
        ->assertJsonPath('data.is_active', false);

    expect(CatalogItem::query()->whereKey($itemId)->value('is_active'))->toBeFalse();
});

it('rejects a duplicate SKU within the same merchant', function () {
    [$merchant, $store] = catalogCtx();
    Sanctum::actingAs(catalogAdmin($store));

    $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/items", [
        'name' => 'A', 'type' => 'product', 'base_price_minor' => 100, 'currency' => 'USD', 'sku' => 'DUP',
    ])->assertCreated();

    $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/items", [
        'name' => 'B', 'type' => 'product', 'base_price_minor' => 100, 'currency' => 'USD', 'sku' => 'DUP',
    ])->assertStatus(422);
});

it('allows the same SKU across different merchants', function () {
    [$mA, $storeA] = catalogCtx('A');
    [$mB, $storeB] = catalogCtx('B');

    Sanctum::actingAs(catalogAdmin($storeA));
    $this->postJson("/api/admin/v1/stores/{$storeA->id}/catalog/items", [
        'name' => 'A', 'type' => 'product', 'base_price_minor' => 100, 'currency' => 'USD', 'sku' => 'SHARED',
    ])->assertCreated();

    Sanctum::actingAs(catalogAdmin($storeB));
    $this->postJson("/api/admin/v1/stores/{$storeB->id}/catalog/items", [
        'name' => 'B', 'type' => 'product', 'base_price_minor' => 100, 'currency' => 'USD', 'sku' => 'SHARED',
    ])->assertCreated();
});

it('manages categories store-scoped', function () {
    [$merchant, $store] = catalogCtx();
    Sanctum::actingAs(catalogAdmin($store));

    $cat = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/categories", [
        'name' => 'Drinks',
        'sort_order' => 1,
    ])->assertCreated()->json('data');

    $this->getJson("/api/admin/v1/stores/{$store->id}/catalog/categories")
        ->assertOk()
        ->assertJsonPath('data.0.name', 'Drinks');

    $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/categories/{$cat['id']}/deactivate")
        ->assertOk()
        ->assertJsonPath('data.is_active', false);
});

it('hides deactivated items from the POS config payload', function () {
    [$merchant, $store] = catalogCtx();
    $admin = catalogAdmin($store);
    Sanctum::actingAs($admin);

    $item = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/items", [
        'name' => 'Hidden', 'type' => 'product', 'base_price_minor' => 100, 'currency' => 'USD', 'sku' => 'HID-1',
    ])->json('data');

    $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/items/{$item['id']}/deactivate")->assertOk();

    // A device for this store reads config; deactivated item should be absent.
    $profile = DeviceProfile::query()->create(['name' => 'P', 'type' => 'tablet', 'capabilities' => []]);
    $device = Device::query()->create([
        'merchant_id' => $merchant->id, 'store_id' => $store->id, 'device_profile_id' => $profile->id,
        'name' => 'Reg', 'platform' => 'android', 'status' => 'active',
    ]);
    Sanctum::actingAs($device, ['pos:access']);

    $config = $this->withHeaders(posHeaders())->getJson('/api/pos/v1/config')->assertOk()->json();
    $skus = collect($config['items'] ?? [])->pluck('sku')->all();

    expect($skus)->not->toContain('HID-1');
});

it('includes barcode and category_name in the POS config item payload', function () {
    [$merchant, $store] = catalogCtx();
    $admin = catalogAdmin($store);
    Sanctum::actingAs($admin);

    $category = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/categories", [
        'name' => 'Beverages',
    ])->json('data');

    $item = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/items", [
        'name' => 'Cola', 'type' => 'product', 'base_price_minor' => 250, 'currency' => 'USD',
        'sku' => 'COLA-1', 'category_id' => $category['id'],
    ])->json('data');

    // Primary barcode lives in the Retail-owned barcode_records table.
    DB::table('barcode_records')->insert([
        'id' => (string) Str::ulid(),
        'merchant_id' => $merchant->id, 'store_id' => $store->id,
        'catalog_item_id' => $item['id'], 'sku' => 'COLA-1',
        'barcode' => '0123456789012', 'is_primary' => true,
        'created_at' => now(), 'updated_at' => now(),
    ]);

    $profile = DeviceProfile::query()->create(['name' => 'P', 'type' => 'tablet', 'capabilities' => []]);
    $device = Device::query()->create([
        'merchant_id' => $merchant->id, 'store_id' => $store->id, 'device_profile_id' => $profile->id,
        'name' => 'Reg', 'platform' => 'android', 'status' => 'active',
    ]);
    Sanctum::actingAs($device, ['pos:access']);

    $config = $this->withHeaders(posHeaders())->getJson('/api/pos/v1/config')->assertOk()->json();
    $payloadItem = collect($config['items'] ?? [])->firstWhere('sku', 'COLA-1');

    expect($payloadItem)->not->toBeNull();
    expect($payloadItem['barcode'])->toBe('0123456789012');
    expect($payloadItem['category_name'])->toBe('Beverages');
});

it('returns 404 updating an item from another merchant', function () {
    [$mA, $storeA] = catalogCtx('A');
    [$mB, $storeB] = catalogCtx('B');

    Sanctum::actingAs(catalogAdmin($storeB));
    $itemB = $this->postJson("/api/admin/v1/stores/{$storeB->id}/catalog/items", [
        'name' => 'B', 'type' => 'product', 'base_price_minor' => 100, 'currency' => 'USD',
    ])->json('data');

    Sanctum::actingAs(catalogAdmin($storeA));
    $this->putJson("/api/admin/v1/stores/{$storeA->id}/catalog/items/{$itemB['id']}", [
        'name' => 'hijack',
    ])->assertNotFound();
});
