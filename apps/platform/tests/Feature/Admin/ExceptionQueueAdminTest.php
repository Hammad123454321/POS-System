<?php

use App\Models\User;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionResolution;
use App\Modules\Identity\Domain\Models\Role;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

it('resolves open exception cases for authorized back-office users', function () {
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

    $case = ExceptionCase::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'module' => 'sync',
        'code' => 'sync_event.recovery_failed',
        'severity' => 'medium',
        'status' => 'open',
        'title' => 'Manual sync review required.',
    ]);

    Sanctum::actingAs($user);

    $this->postJson("/api/admin/v1/exception-cases/{$case->id}/resolve", [
        'resolution_code' => 'recovered',
        'notes' => 'Verified and replayed cleanly.',
    ])->assertOk()
        ->assertJsonPath('data.status', 'resolved')
        ->assertJsonPath('data.resolution.resolution_code', 'recovered');

    expect($case->fresh()->status)->toBe('resolved');
    expect(ExceptionResolution::query()->where('exception_case_id', $case->id)->count())->toBe(1);
});
