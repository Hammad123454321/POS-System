<?php

use App\Models\User;
use App\Modules\Identity\Application\AcceptInvitation;
use App\Modules\Identity\Application\ProvisionMerchantRoles;
use App\Modules\Identity\Domain\Models\UserInvitation;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Mail;
use Laravel\Sanctum\Sanctum;

function seedMerchantStore(string $code = 'M1'): array
{
    $merchant = Merchant::query()->create([
        'name' => 'Merchant '.$code,
        'currency' => 'USD',
        'status' => 'active',
    ]);
    app(ProvisionMerchantRoles::class)->handle($merchant);

    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Store '.$code,
        'code' => $code,
        'mode' => 'restaurant',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    return [$merchant, $store];
}

function ownerOf(Store $store): User
{
    $user = User::factory()->create();
    $roleId = DB::table('roles')->where('merchant_id', $store->merchant_id)->where('name', 'Merchant Owner')->value('id');
    DB::table('user_store_role')->insert([
        'user_id' => $user->id,
        'store_id' => $store->id,
        'role_id' => $roleId,
        'created_at' => now(),
        'updated_at' => now(),
    ]);

    return $user;
}

function roleId(Store $store, string $name): string
{
    return DB::table('roles')->where('merchant_id', $store->merchant_id)->where('name', $name)->value('id');
}

it('invites a user, who can accept and gain the role', function () {
    Mail::fake();
    [$merchant, $store] = seedMerchantStore();
    $owner = ownerOf($store);
    Sanctum::actingAs($owner);

    $this->postJson("/api/admin/v1/stores/{$store->id}/users/invitations", [
        'email' => 'newhire@example.com',
        'role_id' => roleId($store, 'Cashier'),
    ])->assertCreated();

    $invitation = UserInvitation::query()->where('email', 'newhire@example.com')->firstOrFail();

    // Recover the plaintext token by re-inviting through the action (fake mail captures token).
    // Simpler: directly accept using a known token via the action under test.
    $token = 'known-token-value-1234567890';
    $invitation->forceFill(['token_hash' => hash('sha256', $token)])->save();

    $user = app(AcceptInvitation::class)->handle($token, 'New Hire', 'password123');

    expect($user->email)->toBe('newhire@example.com');
    expect(DB::table('user_store_role')->where('user_id', $user->id)->where('store_id', $store->id)->exists())->toBeTrue();
    expect($invitation->fresh()->accepted_at)->not->toBeNull();
});

it('rejects an already-accepted invitation token', function () {
    [$merchant, $store] = seedMerchantStore();
    $token = 'token-abc';

    UserInvitation::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'role_id' => roleId($store, 'Cashier'),
        'email' => 'used@example.com',
        'token_hash' => hash('sha256', $token),
        'expires_at' => now()->addDay(),
        'accepted_at' => now(),
    ]);

    expect(fn () => app(AcceptInvitation::class)->handle($token, 'X', 'password123'))
        ->toThrow(DomainException::class);
});

it('rejects an expired invitation token', function () {
    [$merchant, $store] = seedMerchantStore();
    $token = 'token-exp';

    UserInvitation::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'role_id' => roleId($store, 'Cashier'),
        'email' => 'late@example.com',
        'token_hash' => hash('sha256', $token),
        'expires_at' => now()->subDay(),
    ]);

    expect(fn () => app(AcceptInvitation::class)->handle($token, 'X', 'password123'))
        ->toThrow(DomainException::class);
});

it('prevents revoking the last Merchant Owner', function () {
    [$merchant, $store] = seedMerchantStore();
    $owner = ownerOf($store);
    Sanctum::actingAs($owner);

    $ownerRole = roleId($store, 'Merchant Owner');

    $this->deleteJson("/api/admin/v1/stores/{$store->id}/users/{$owner->id}/roles/{$ownerRole}")
        ->assertStatus(422);

    expect(DB::table('user_store_role')->where('user_id', $owner->id)->exists())->toBeTrue();
});

it('assigns and revokes a non-owner role', function () {
    [$merchant, $store] = seedMerchantStore();
    $owner = ownerOf($store);
    $target = User::factory()->create();
    Sanctum::actingAs($owner);

    $cashier = roleId($store, 'Cashier');

    $this->postJson("/api/admin/v1/stores/{$store->id}/users/{$target->id}/roles", [
        'role_id' => $cashier,
    ])->assertOk();

    expect(DB::table('user_store_role')->where('user_id', $target->id)->where('role_id', $cashier)->exists())->toBeTrue();

    $this->deleteJson("/api/admin/v1/stores/{$store->id}/users/{$target->id}/roles/{$cashier}")
        ->assertOk();

    expect(DB::table('user_store_role')->where('user_id', $target->id)->where('role_id', $cashier)->exists())->toBeFalse();
});

it('forbids inviting without users.manage permission', function () {
    [$merchant, $store] = seedMerchantStore();

    // Cashier lacks users.manage.
    $user = User::factory()->create();
    DB::table('user_store_role')->insert([
        'user_id' => $user->id,
        'store_id' => $store->id,
        'role_id' => roleId($store, 'Cashier'),
        'created_at' => now(),
        'updated_at' => now(),
    ]);
    Sanctum::actingAs($user);

    $this->postJson("/api/admin/v1/stores/{$store->id}/users/invitations", [
        'email' => 'x@example.com',
        'role_id' => roleId($store, 'Cashier'),
    ])->assertForbidden();
});
