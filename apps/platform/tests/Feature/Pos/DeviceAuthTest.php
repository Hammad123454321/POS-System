<?php

use App\Models\User;
use App\Modules\Identity\Domain\Models\Role;
use App\Modules\PlatformCore\Application\DeviceAuth\CreateEnrollmentCode;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\DeviceRefreshToken;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

it('creates device enrollment codes for authorized store admins', function () {
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

    $profile = DeviceProfile::query()->create([
        'name' => 'Counter POS',
        'type' => 'countertop',
        'capabilities' => ['receipt_printer' => true],
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

    $response = $this->postJson("/api/admin/v1/stores/{$store->id}/device-enrollment-codes", [
        'device_profile_id' => $profile->id,
    ]);

    $response->assertCreated()
        ->assertJsonPath('data.store_id', $store->id)
        ->assertJsonPath('data.device_profile_id', $profile->id);

    expect($response->json('data.code'))->not->toBeEmpty();
});

it('enrolls a device, allows bootstrap, and rotates refresh tokens', function () {
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

    $profile = DeviceProfile::query()->create([
        'name' => 'Counter POS',
        'type' => 'countertop',
        'capabilities' => ['receipt_printer' => true],
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

    $enrollmentCode = app(CreateEnrollmentCode::class)
        ->handle($user, $store, $profile->id)['code'];
    $androidIdentity = fakeAndroidEnrollmentIdentity();

    $enrollResponse = $this->withHeaders(posHeaders())->postJson('/api/pos/v1/auth/enroll', [
        'enrollment_code' => $enrollmentCode,
        'device_name' => 'Front Register',
        'platform' => 'android',
        'device_fingerprint' => 'fingerprint-001',
        'public_key' => $androidIdentity['public_key'],
        'attestation' => [
            'provider' => 'android_keystore',
            'certificate_chain' => [$androidIdentity['certificate']],
        ],
    ]);

    $enrollResponse->assertCreated()
        ->assertJsonPath('data.device.store_id', $store->id)
        ->assertJsonPath('data.auth.silent_refresh_window_minutes', 5);

    $accessToken = $enrollResponse->json('data.auth.access_token');
    $refreshToken = $enrollResponse->json('data.auth.refresh_token');
    $deviceId = $enrollResponse->json('data.device.id');

    $tokenableId = DB::table('personal_access_tokens')
        ->where('tokenable_type', Device::class)
        ->value('tokenable_id');
    expect($tokenableId)->toBe($deviceId);

    $this->withHeaders(posHeaders([
        'Authorization' => 'Bearer '.$accessToken,
    ]))
        ->getJson('/api/pos/v1/bootstrap')
        ->assertOk()
        ->assertJsonPath('store.id', $store->id);

    $refreshResponse = $this->withHeaders(posHeaders())
        ->postJson('/api/pos/v1/auth/refresh', [
            'refresh_token' => $refreshToken,
        ]);

    $refreshResponse->assertOk()
        ->assertJsonPath('data.device.status', 'active');

    $rotatedAccessToken = $refreshResponse->json('data.auth.access_token');

    $this->withHeaders(posHeaders([
        'Authorization' => 'Bearer '.$rotatedAccessToken,
    ]))
        ->getJson('/api/pos/v1/bootstrap')
        ->assertOk();

    $this->withHeaders(posHeaders())
        ->postJson('/api/pos/v1/auth/refresh', [
            'refresh_token' => $refreshToken,
        ])
        ->assertStatus(401)
        ->assertJsonPath('error_code', 'DEVICE_REAUTH_REQUIRED');

    $device = Device::query()->firstOrFail();

    expect($device->tokens()->count())->toBe(0);
    expect(
        DeviceRefreshToken::query()
            ->where('device_id', $device->id)
            ->whereNotNull('revoked_at')
            ->count()
    )->toBeGreaterThanOrEqual(2);
});

it('rejects non-android device enrollment requests', function () {
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

    $profile = DeviceProfile::query()->create([
        'name' => 'Counter POS',
        'type' => 'countertop',
        'capabilities' => ['receipt_printer' => true],
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

    $enrollmentCode = app(CreateEnrollmentCode::class)
        ->handle($user, $store, $profile->id)['code'];
    $androidIdentity = fakeAndroidEnrollmentIdentity();

    $this->withHeaders(posHeaders())->postJson('/api/pos/v1/auth/enroll', [
        'enrollment_code' => $enrollmentCode,
        'device_name' => 'Front Register',
        'platform' => 'windows',
        'device_fingerprint' => 'fingerprint-001',
        'public_key' => $androidIdentity['public_key'],
        'attestation' => [
            'provider' => 'android_keystore',
            'certificate_chain' => [$androidIdentity['certificate']],
        ],
    ])
        ->assertStatus(422)
        ->assertJsonPath('errors.platform.0', 'The selected platform is invalid.');
});

/**
 * @return array{public_key: string, certificate: string}
 */
function fakeAndroidEnrollmentIdentity(): array
{
    $certificateBase64 = 'MIIDDjCCAfagAwIBAgIQHXxniMCYCL5LDPdvIKK+gzANBgkqhkiG9w0BAQsFADAaMRgwFgYDVQQDDA9QT1MgVGVzdCBEZXZpY2UwHhcNMjYwNDIyMTAyMTIwWhcNMjcwNDIyMTA0MTIwWjAaMRgwFgYDVQQDDA9QT1MgVGVzdCBEZXZpY2UwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC7I1e8PcjDgZddvuQ6snqbXq1DJ/q4jUKNedwwVk9P5JJ4cm5foAR2tv4YlnaB7ONeXBLPLJz5tra0LxyF4dBpjxNrCnIykii5rIVDurhL5oR845t7LvDGHK8Ev1IJFw19D7IKQUVeRkLpjH2VgVMcwZ7FxjPmBT+eFZatXBQud9OGLw2tn3RBM0Hk9x7jSHlC5C1OfAiuqxOvD8ZuwdWSNYkQ4zCQjdCnCKuedbM+9dBodbeTuBuVfev7pwzNaN+pb8gsA15KwuuCX05c8srlev8aJqDZlWq9ftnirRmsMDEMewXUQ7CicuDSVHu1f5AnXgLu7eFNj4fAl2VGcnClAgMBAAGjUDBOMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAgYIKwYBBQUHAwEwHQYDVR0OBBYEFGzPAL8vX2AE14FJOkJ/tdcIDjbjMA0GCSqGSIb3DQEBCwUAA4IBAQAnYQ25TIObiAsj4cnNd7h+gcMBtSu3ImoaAyFjUXtcTREbZkLhojWEAS5O89xw5DFwDOQht68433iY3Vz7mNbdJUlMujnGCzvYj6wp0ucnF4VEBy8SFKhNEwWxPjG/wBNxoM+L0Ofo2XjvT+RQyqpyKD+8kZCHL0OwIvKNkYwzT0y1tEH2WBDIaKOs045s7oHtD+e+nPv0YX4hAjxvhlXHA270ev/vFP0SiSPrisSfBsCk0Y3MXDMjb8vso4CQOig07Da9w0fP8kpmaSFwkC2w6jkvTlIwSqLdWWt2YF1DM0DXApKv18gUB5SV1JoLKR6tDAc1IhYsGYp3IS6iaFBa';
    $certificatePem = "-----BEGIN CERTIFICATE-----\n"
        .chunk_split($certificateBase64, 64, "\n")
        ."-----END CERTIFICATE-----\n";
    $certificate = openssl_x509_read($certificatePem);

    expect($certificate)->not->toBeFalse();

    $publicKey = openssl_pkey_get_public($certificate);
    expect($publicKey)->not->toBeFalse();

    $publicKeyDetails = openssl_pkey_get_details($publicKey);
    expect($publicKeyDetails)->toBeArray();

    return [
        'public_key' => pemToBase64Der((string) $publicKeyDetails['key']),
        'certificate' => $certificateBase64,
    ];
}

function pemToBase64Der(string $pem): string
{
    return (string) preg_replace('/-----BEGIN [^-]+-----|-----END [^-]+-----|\s+/', '', $pem);
}
