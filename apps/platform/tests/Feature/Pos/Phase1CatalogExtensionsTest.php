<?php

use App\Models\User;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\Category;
use App\Modules\Catalog\Domain\Models\ComboPackage;
use App\Modules\Catalog\Domain\Models\ModifierGroup;
use App\Modules\Catalog\Domain\Models\ModifierOption;
use App\Modules\Catalog\Domain\Models\TaxRule;
use App\Modules\Catalog\Domain\Models\Variant;
use App\Modules\Identity\Domain\Models\Role;
use App\Modules\OrderRegister\Domain\Models\OrderLine;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

function buildCatalogExtensionAdminContext(): array
{
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

    return [$merchant, $store];
}

function createCatalogItemForStore(Merchant $merchant, Store $store, string $name, string $sku, int $priceMinor): CatalogItem
{
    $taxRule = TaxRule::query()->firstOrCreate(
        ['merchant_id' => $merchant->id, 'code' => 'STD'],
        [
            'name' => 'Sales Tax',
            'rate_basis_points' => 1000,
            'is_inclusive' => false,
            'is_active' => true,
        ],
    );

    $category = Category::query()->firstOrCreate(
        ['merchant_id' => $merchant->id, 'store_id' => $store->id, 'name' => 'Food'],
        ['sort_order' => 1, 'is_active' => true],
    );

    return CatalogItem::query()->create([
        'merchant_id' => $merchant->id,
        'category_id' => $category->id,
        'tax_rule_id' => $taxRule->id,
        'type' => 'product',
        'name' => $name,
        'sku' => $sku,
        'base_price_minor' => $priceMinor,
        'currency' => 'USD',
        'is_active' => true,
        'sold_out' => false,
    ]);
}

it('creates variants, modifier groups/options, combo packages, and add-on mappings via admin apis', function () {
    [$merchant, $store] = buildCatalogExtensionAdminContext();

    $burger = createCatalogItemForStore($merchant, $store, 'Burger', 'BRG-01', 1200);
    $fries = createCatalogItemForStore($merchant, $store, 'Fries', 'FRY-01', 500);
    $drink = createCatalogItemForStore($merchant, $store, 'Drink', 'DRK-01', 300);

    $variant = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/variants", [
        'catalog_item_id' => $burger->id,
        'name' => 'Large',
        'code' => 'LG',
        'options' => ['size' => 'large'],
        'price_delta_minor' => 200,
        'currency' => 'USD',
    ])->assertCreated()->json('data');

    $group = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/modifier-groups", [
        'catalog_item_id' => $burger->id,
        'name' => 'Sauces',
        'selection_mode' => 'multi',
        'min_select' => 0,
        'max_select' => 2,
    ])->assertCreated()->json('data');

    $option = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/modifier-options", [
        'modifier_group_id' => $group['id'],
        'name' => 'Cheese Sauce',
        'code' => 'CHEESE',
        'price_delta_minor' => 100,
        'currency' => 'USD',
    ])->assertCreated()->json('data');

    $combo = $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/combo-packages", [
        'name' => 'Burger Combo',
        'code' => 'COMBO-1',
        'price_minor' => 1500,
        'currency' => 'USD',
        'items' => [
            ['catalog_item_id' => $burger->id, 'quantity' => 1],
            ['catalog_item_id' => $fries->id, 'quantity' => 1],
        ],
        'add_on_item_ids' => [$drink->id],
    ])->assertCreated()->json('data');

    $this->postJson("/api/admin/v1/stores/{$store->id}/catalog/items/{$burger->id}/add-ons", [
        'add_on_item_ids' => [$drink->id],
    ])->assertCreated();

    expect(Variant::query()->find($variant['id']))->not->toBeNull();
    expect(ModifierGroup::query()->find($group['id']))->not->toBeNull();
    expect(ModifierOption::query()->find($option['id']))->not->toBeNull();
    expect(ComboPackage::query()->find($combo['id']))->not->toBeNull();
});

it('includes variants modifiers combos and add-ons in pos config catalog snapshot', function () {
    [$merchant, $store] = buildCatalogExtensionAdminContext();

    $burger = createCatalogItemForStore($merchant, $store, 'Burger', 'BRG-01', 1200);
    $fries = createCatalogItemForStore($merchant, $store, 'Fries', 'FRY-01', 500);
    $drink = createCatalogItemForStore($merchant, $store, 'Drink', 'DRK-01', 300);

    $variant = Variant::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'catalog_item_id' => $burger->id,
        'name' => 'Large',
        'code' => 'LG',
        'options' => ['size' => 'large'],
        'price_delta_minor' => 200,
        'currency' => 'USD',
        'is_active' => true,
    ]);

    $group = ModifierGroup::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'catalog_item_id' => $burger->id,
        'name' => 'Sauces',
        'selection_mode' => 'multi',
        'min_select' => 0,
        'max_select' => 2,
        'is_required' => false,
        'is_active' => true,
    ]);

    ModifierOption::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'modifier_group_id' => $group->id,
        'name' => 'Cheese Sauce',
        'price_delta_minor' => 100,
        'currency' => 'USD',
        'is_active' => true,
    ]);

    $combo = ComboPackage::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'name' => 'Burger Combo',
        'code' => 'COMBO-1',
        'price_minor' => 1500,
        'currency' => 'USD',
        'is_active' => true,
    ]);

    DB::table('combo_package_items')->insert([
        'id' => (string) str()->ulid(),
        'merchant_id' => $merchant->id,
        'combo_package_id' => $combo->id,
        'catalog_item_id' => $fries->id,
        'quantity' => 1,
        'created_at' => now(),
        'updated_at' => now(),
    ]);
    DB::table('combo_package_add_ons')->insert([
        'id' => (string) str()->ulid(),
        'merchant_id' => $merchant->id,
        'combo_package_id' => $combo->id,
        'catalog_item_id' => $drink->id,
        'created_at' => now(),
        'updated_at' => now(),
    ]);
    DB::table('catalog_item_add_ons')->insert([
        'id' => (string) str()->ulid(),
        'merchant_id' => $merchant->id,
        'catalog_item_id' => $burger->id,
        'add_on_item_id' => $drink->id,
        'created_at' => now(),
        'updated_at' => now(),
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
        'platform' => 'android',
        'status' => 'active',
        'device_fingerprint' => 'fingerprint-123',
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $response = $this->withHeaders(posHeaders())->getJson('/api/pos/v1/config')
        ->assertOk()
        ->json();

    $burgerSnapshot = collect($response['items'] ?? [])
        ->firstWhere('id', $burger->id);

    expect($burgerSnapshot['variants'][0]['id'] ?? null)->toBe($variant->id);
    expect($burgerSnapshot['modifier_groups'][0]['id'] ?? null)->toBe($group->id);
    expect($response['combo_packages'][0]['id'] ?? null)->toBe($combo->id);
});

it('applies variant modifier combo and add-on selections to order totals and snapshots', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();
    $item->forceFill(['sku' => 'BRG-01'])->save();

    $fries = CatalogItem::query()->create([
        'merchant_id' => $device->merchant_id,
        'category_id' => $item->category_id,
        'tax_rule_id' => $item->tax_rule_id,
        'type' => 'product',
        'name' => 'Fries',
        'sku' => 'FRY-01',
        'base_price_minor' => 500,
        'currency' => 'USD',
        'is_active' => true,
        'sold_out' => false,
    ]);
    $drink = CatalogItem::query()->create([
        'merchant_id' => $device->merchant_id,
        'category_id' => $item->category_id,
        'tax_rule_id' => $item->tax_rule_id,
        'type' => 'product',
        'name' => 'Drink',
        'sku' => 'DRK-01',
        'base_price_minor' => 300,
        'currency' => 'USD',
        'is_active' => true,
        'sold_out' => false,
    ]);

    $variant = Variant::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'catalog_item_id' => $item->id,
        'name' => 'Large',
        'price_delta_minor' => 200,
        'is_active' => true,
    ]);
    $group = ModifierGroup::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'catalog_item_id' => $item->id,
        'name' => 'Sauces',
        'selection_mode' => 'multi',
        'min_select' => 0,
        'max_select' => 2,
        'is_active' => true,
    ]);
    $option = ModifierOption::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'modifier_group_id' => $group->id,
        'name' => 'Cheese Sauce',
        'price_delta_minor' => 100,
        'is_active' => true,
    ]);
    $combo = ComboPackage::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'name' => 'Combo',
        'price_minor' => 1500,
        'currency' => 'USD',
        'is_active' => true,
    ]);
    DB::table('combo_package_add_ons')->insert([
        'id' => (string) str()->ulid(),
        'merchant_id' => $device->merchant_id,
        'combo_package_id' => $combo->id,
        'catalog_item_id' => $drink->id,
        'created_at' => now(),
        'updated_at' => now(),
    ]);
    DB::table('catalog_item_add_ons')->insert([
        'id' => (string) str()->ulid(),
        'merchant_id' => $device->merchant_id,
        'catalog_item_id' => $item->id,
        'add_on_item_id' => $drink->id,
        'created_at' => now(),
        'updated_at' => now(),
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $response = $this->withHeaders(posHeaders() + [
        'Idempotency-Key' => 'catalog-extension-order-1',
    ])
        ->postJson('/api/pos/v1/orders', [
            'register_session_id' => $registerSession->id,
            'lines' => [[
                'catalog_item_id' => $item->id,
                'quantity' => 1,
                'variant_id' => $variant->id,
                'modifier_option_ids' => [$option->id],
                'combo_package_id' => $combo->id,
                'add_on_item_ids' => [$drink->id],
            ]],
        ])
        ->assertCreated()
        ->json('data');

    expect($response['subtotal_minor'])->toBe(1900);

    $line = OrderLine::query()->firstOrFail();
    expect($line->selection_snapshot['variant']['id'] ?? null)->toBe($variant->id);
    expect($line->selection_snapshot['combo']['id'] ?? null)->toBe($combo->id);
    expect($line->selection_snapshot['modifiers'][0]['id'] ?? null)->toBe($option->id);
});
