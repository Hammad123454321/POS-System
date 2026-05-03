<?php

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Restaurant\Domain\Models\DiningTable;
use App\Modules\Restaurant\Domain\Models\TableAssignment;
use Carbon\CarbonImmutable;
use Laravel\Sanctum\Sanctum;

it('claims table leases, blocks competing devices, and recovers control after lease reaping', function () {
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
        'name' => 'Floor Handheld',
        'platform' => 'android',
        'status' => 'active',
        'drawer_code' => 'drawer-2',
    ]);

    $table = DiningTable::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'name' => 'T1',
        'zone_name' => 'Main',
        'capacity' => 4,
        'sort_order' => 1,
        'is_active' => true,
    ]);

    TableAssignment::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'dining_table_id' => $table->id,
        'status' => 'available',
    ]);

    Sanctum::actingAs($deviceA, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'table-claim-1',
    ]))->postJson("/api/pos/v1/restaurant/tables/{$table->id}/claim", [
        'current_party_name' => 'Smith',
        'guest_count' => 3,
    ])->assertOk()
        ->assertJsonPath('data.status', 'occupied')
        ->assertJsonPath('data.current_party_name', 'Smith')
        ->assertJsonPath('data.lease.current_holder_device_id', $deviceA->id);

    Sanctum::actingAs($deviceB, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'table-claim-2',
    ]))->postJson("/api/pos/v1/restaurant/tables/{$table->id}/claim", [
        'current_party_name' => 'Johnson',
        'guest_count' => 2,
    ])->assertStatus(409)
        ->assertJsonPath('error_code', 'LEASE_CONFLICT');

    $lease = EditLease::query()->where('resource_type', 'table_assignment')->where('resource_id', $table->id)->firstOrFail();
    $lease->forceFill([
        'lease_expires_at' => CarbonImmutable::now('UTC')->subSecond(),
    ])->save();

    $this->artisan('pos:reap-edit-leases')->assertExitCode(0);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'table-claim-3',
    ]))->postJson("/api/pos/v1/restaurant/tables/{$table->id}/claim", [
        'current_party_name' => 'Johnson',
        'guest_count' => 2,
    ])->assertOk()
        ->assertJsonPath('data.status', 'occupied')
        ->assertJsonPath('data.current_party_name', 'Smith')
        ->assertJsonPath('data.assigned_device_id', $deviceB->id);

    $this->withHeaders(posHeaders())->getJson('/api/pos/v1/restaurant/tables')
        ->assertOk()
        ->assertJsonPath('tables.0.name', 'T1')
        ->assertJsonPath('tables.0.lease.current_holder_device_id', $deviceB->id);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'table-release-1',
    ]))->postJson("/api/pos/v1/restaurant/tables/{$table->id}/release")
        ->assertOk()
        ->assertJsonPath('data.status', 'available');
});
