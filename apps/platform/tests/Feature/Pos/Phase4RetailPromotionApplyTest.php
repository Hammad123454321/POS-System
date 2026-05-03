<?php

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\Category;
use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\Catalog\Domain\Models\TaxRule;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\OrderLine;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Domain\Models\BarcodeRecord;
use Laravel\Sanctum\Sanctum;

function buildRetailPosOrderContext(): array
{
    $merchant = Merchant::query()->create([
        'name' => 'Retail Merchant',
        'currency' => 'USD',
        'status' => 'active',
    ]);

    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Retail Store',
        'code' => 'RTL-1',
        'mode' => 'retail',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    $profile = DeviceProfile::query()->create([
        'name' => 'Retail Counter',
        'type' => 'countertop',
        'capabilities' => ['receipt_printer' => true],
    ]);

    $device = Device::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_profile_id' => $profile->id,
        'name' => 'Retail POS',
        'platform' => 'windows',
        'status' => 'active',
        'drawer_code' => 'drawer-r1',
    ]);

    $taxRule = TaxRule::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Sales Tax',
        'code' => 'STD',
        'rate_basis_points' => 1000,
        'is_inclusive' => false,
        'is_active' => true,
    ]);

    $category = Category::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'name' => 'Retail',
        'sort_order' => 1,
        'is_active' => true,
    ]);

    $item = CatalogItem::query()->create([
        'merchant_id' => $merchant->id,
        'category_id' => $category->id,
        'tax_rule_id' => $taxRule->id,
        'type' => 'product',
        'name' => 'Retail Item',
        'sku' => 'RTL-SKU-1',
        'base_price_minor' => 1000,
        'currency' => 'USD',
        'is_active' => true,
        'sold_out' => false,
    ]);

    BarcodeRecord::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'catalog_item_id' => $item->id,
        'sku' => $item->sku,
        'barcode' => 'RTL-BC-1',
        'is_primary' => true,
    ]);

    $registerSession = RegisterSession::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_id' => $device->id,
        'drawer_code' => 'drawer-r1',
        'business_date' => '2026-04-27',
        'status' => 'open',
        'session_version' => 1,
        'opening_float_minor' => 10000,
        'expected_cash_minor' => 10000,
        'opened_at' => now(),
    ]);

    return [$device, $registerSession, $item, $category];
}

it('applies highest priority active retail promotion automatically and persists snapshots', function () {
    [$device, $registerSession, $item, $category] = buildRetailPosOrderContext();

    DiscountRule::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'name' => 'Lower Priority Promo',
        'code' => 'LOW',
        'type' => 'percent_basis_points',
        'scope_mode' => 'retail',
        'value_basis_points' => 500,
        'sort_order' => 10,
        'is_active' => true,
        'applicability' => ['skus' => [$item->sku]],
        'is_stackable' => false,
    ]);

    $winner = DiscountRule::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'name' => 'Higher Priority Promo',
        'code' => 'HIGH',
        'type' => 'percent_basis_points',
        'scope_mode' => 'retail',
        'value_basis_points' => 1500,
        'sort_order' => 100,
        'is_active' => true,
        'applicability' => [
            'skus' => [$item->sku],
            'barcodes' => ['RTL-BC-1'],
            'category_ids' => [$category->id],
        ],
        'is_stackable' => false,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $orderPayload = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-retail-promo-order',
    ]))->postJson('/api/pos/v1/orders', [
        'register_session_id' => $registerSession->id,
        'lines' => [
            ['catalog_item_id' => $item->id, 'quantity' => 1],
        ],
    ])->assertCreated()
        ->json('data');

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/config')
        ->assertOk()
        ->assertJsonStructure(['retail_promotions']);

    $order = Order::query()->findOrFail($orderPayload['id']);
    $line = OrderLine::query()->where('order_id', $order->id)->firstOrFail();

    expect($order->discount_rule_id)->toBe($winner->id);
    expect((int) $order->discount_minor)->toBe(150);
    expect((string) ($order->discount_snapshot['id'] ?? ''))->toBe($winner->id);
    expect((string) ($line->discount_snapshot['id'] ?? ''))->toBe($winner->id);
});
