<?php

use App\Models\User;
use App\Modules\Identity\Application\ProvisionMerchantRoles;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

function ordersBrowserContext(string $code = 'OB'): array
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

    $profile = DeviceProfile::query()->create(['name' => 'P '.$code, 'type' => 'tablet', 'capabilities' => []]);
    $device = Device::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_profile_id' => $profile->id,
        'name' => 'Reg '.$code,
        'platform' => 'android',
        'status' => 'active',
    ]);

    $session = RegisterSession::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'device_id' => $device->id,
        'drawer_code' => 'd1',
        'business_date' => '2026-05-01',
        'status' => 'open',
        'session_version' => 1,
        'opening_float_minor' => 0,
        'expected_cash_minor' => 0,
        'opened_at' => now(),
    ]);

    return [$merchant, $store, $device, $session];
}

function makeOrder(Merchant $m, Store $s, Device $d, RegisterSession $sess, string $number, string $status, string $businessDate): Order
{
    return Order::query()->create([
        'merchant_id' => $m->id,
        'store_id' => $s->id,
        'register_session_id' => $sess->id,
        'device_id' => $d->id,
        'order_number' => $number,
        'status' => $status,
        'business_date' => $businessDate,
        'currency' => 'USD',
        'subtotal_minor' => 1000,
        'tax_minor' => 100,
        'discount_minor' => 0,
        'total_minor' => 1100,
        'paid_minor' => $status === 'paid' ? 1100 : 0,
        'opened_at' => now(),
    ]);
}

function adminUser(Store $store, string $role = 'Store Admin'): User
{
    $user = User::factory()->create();
    $roleId = DB::table('roles')->where('merchant_id', $store->merchant_id)->where('name', $role)->value('id');
    DB::table('user_store_role')->insert([
        'user_id' => $user->id, 'store_id' => $store->id, 'role_id' => $roleId,
        'created_at' => now(), 'updated_at' => now(),
    ]);

    return $user;
}

it('lists paginated orders with filters and meta', function () {
    [$m, $store, $device, $session] = ordersBrowserContext();
    makeOrder($m, $store, $device, $session, 'A-001', 'paid', '2026-05-01');
    makeOrder($m, $store, $device, $session, 'A-002', 'open', '2026-05-02');
    makeOrder($m, $store, $device, $session, 'A-003', 'paid', '2026-05-03');

    Sanctum::actingAs(adminUser($store));

    $this->getJson("/api/admin/v1/stores/{$store->id}/orders?per_page=2")
        ->assertOk()
        ->assertJsonPath('meta.per_page', 2)
        ->assertJsonPath('meta.total', 3)
        ->assertJsonCount(2, 'data');

    $this->getJson("/api/admin/v1/stores/{$store->id}/orders?status=paid")
        ->assertOk()
        ->assertJsonPath('meta.total', 2);

    $this->getJson("/api/admin/v1/stores/{$store->id}/orders?business_date_from=2026-05-02&business_date_to=2026-05-03")
        ->assertOk()
        ->assertJsonPath('meta.total', 2);
});

it('shows a single order detail', function () {
    [$m, $store, $device, $session] = ordersBrowserContext();
    $order = makeOrder($m, $store, $device, $session, 'A-009', 'paid', '2026-05-01');

    Sanctum::actingAs(adminUser($store));

    $this->getJson("/api/admin/v1/stores/{$store->id}/orders/{$order->id}")
        ->assertOk()
        ->assertJsonPath('data.order_number', 'A-009')
        ->assertJsonPath('data.total_minor', 1100);
});

it('returns 404 for an order from another store', function () {
    [$mA, $storeA, $dA, $sessA] = ordersBrowserContext('A');
    [$mB, $storeB, $dB, $sessB] = ordersBrowserContext('B');
    $orderB = makeOrder($mB, $storeB, $dB, $sessB, 'B-001', 'paid', '2026-05-01');

    Sanctum::actingAs(adminUser($storeA));

    $this->getJson("/api/admin/v1/stores/{$storeA->id}/orders/{$orderB->id}")
        ->assertNotFound();
});

it('forbids a user without orders.view', function () {
    [$m, $store] = ordersBrowserContext();

    // A user with a role in a *different* merchant has no access here.
    [$m2, $store2] = ordersBrowserContext('X');
    Sanctum::actingAs(adminUser($store2));

    $this->getJson("/api/admin/v1/stores/{$store->id}/orders")
        ->assertForbidden();
});
