<?php

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\Category;
use App\Modules\Catalog\Domain\Models\TaxRule;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

/*
|--------------------------------------------------------------------------
| Test Case
|--------------------------------------------------------------------------
|
| The closure you provide to your test functions is always bound to a specific PHPUnit test
| case class. By default, that class is "PHPUnit\Framework\TestCase". Of course, you may
| need to change it using the "pest()" function to bind different classes or traits.
|
*/

pest()->extend(TestCase::class)
    ->use(RefreshDatabase::class)
    ->in('Feature');

/*
|--------------------------------------------------------------------------
| Expectations
|--------------------------------------------------------------------------
|
| When you're writing tests, you often need to check that values meet certain conditions. The
| "expect()" function gives you access to a set of "expectations" methods that you can use
| to assert different things. Of course, you may extend the Expectation API at any time.
|
*/

expect()->extend('toBeOne', fn () => $this->toBe(1));

function posHeaders(array $overrides = []): array
{
    return $overrides + [
        'X-POS-App-Version' => '0.1.0',
        'X-Device-Protocol-Version' => '1',
        'X-Platform' => 'windows',
    ];
}

function buildPosOrderContext(): array
{
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
        'name' => 'Food',
        'sort_order' => 1,
        'is_active' => true,
    ]);

    $item = CatalogItem::query()->create([
        'merchant_id' => $merchant->id,
        'category_id' => $category->id,
        'tax_rule_id' => $taxRule->id,
        'type' => 'product',
        'name' => 'Burger',
        'sku' => 'BRG-01',
        'base_price_minor' => 1200,
        'currency' => 'USD',
        'is_active' => true,
        'sold_out' => false,
    ]);

    $registerSession = RegisterSession::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_id' => $device->id,
        'drawer_code' => 'drawer-1',
        'business_date' => '2026-04-20',
        'status' => 'open',
        'session_version' => 1,
        'opening_float_minor' => 10000,
        'expected_cash_minor' => 10000,
        'opened_at' => now(),
    ]);

    return [$device, $registerSession, $item];
}
