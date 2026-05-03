<?php

use App\Models\User;
use App\Modules\Identity\Domain\Models\Role;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

function buildPhase2AdminContext(): array
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

it('creates membership plans for authorized store admins', function () {
    [, , $store] = buildPhase2AdminContext();

    $this->postJson("/api/admin/v1/stores/{$store->id}/membership-plans", [
        'name' => 'Monthly VIP',
        'code' => 'VIP-MONTHLY',
        'price_minor' => 2999,
        'duration_days' => 30,
        'benefits_snapshot' => [
            'discount_basis_points' => 1000,
        ],
    ])->assertCreated()
        ->assertJsonPath('data.code', 'VIP-MONTHLY')
        ->assertJsonPath('data.price_minor', 2999)
        ->assertJsonPath('data.duration_days', 30);
});
