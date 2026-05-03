<?php

use App\Models\User;
use App\Modules\Identity\Domain\Models\Role;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

function buildAdminStoreContext(): array
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

    return [$user, $merchant, $store];
}

it('creates customers with optional member basics for authorized store admins', function () {
    [, , $store] = buildAdminStoreContext();

    $this->postJson("/api/admin/v1/stores/{$store->id}/customers", [
        'name' => 'VIP Guest',
        'phone' => '+15550001111',
        'email' => 'vip@example.test',
        'member_number' => 'MEM-1001',
    ])->assertCreated()
        ->assertJsonPath('data.name', 'VIP Guest')
        ->assertJsonPath('data.member_account.member_number', 'MEM-1001');
});

it('creates discount rules for authorized store admins', function () {
    [, , $store] = buildAdminStoreContext();

    $this->postJson("/api/admin/v1/stores/{$store->id}/discount-rules", [
        'name' => 'VIP 10%',
        'code' => 'VIP10',
        'type' => 'percent_basis_points',
        'value_basis_points' => 1000,
    ])->assertCreated()
        ->assertJsonPath('data.code', 'VIP10')
        ->assertJsonPath('data.value_basis_points', 1000);
});

it('creates printer configs and print routes for authorized store admins', function () {
    [, , $store] = buildAdminStoreContext();

    $primaryPrinter = $this->postJson("/api/admin/v1/stores/{$store->id}/printer-configs", [
        'name' => 'Front Receipt Printer',
        'driver_key' => 'network-escpos',
        'connection_config' => [
            'host' => '192.168.1.50',
            'port' => 9100,
        ],
    ])->assertCreated()
        ->json('data');

    $secondaryPrinter = $this->postJson("/api/admin/v1/stores/{$store->id}/printer-configs", [
        'name' => 'Backup Receipt Printer',
        'driver_key' => 'network-escpos',
        'connection_config' => [
            'host' => '192.168.1.51',
            'port' => 9100,
        ],
    ])->assertCreated()
        ->json('data');

    $this->postJson("/api/admin/v1/stores/{$store->id}/print-routes", [
        'route_key' => 'receipt-default',
        'document_type' => 'receipt',
        'primary_printer_config_id' => $primaryPrinter['id'],
        'secondary_printer_config_id' => $secondaryPrinter['id'],
    ])->assertCreated()
        ->assertJsonPath('data.route_key', 'receipt-default')
        ->assertJsonPath('data.primary_printer.name', 'Front Receipt Printer')
        ->assertJsonPath('data.secondary_printer.name', 'Backup Receipt Printer');
});
