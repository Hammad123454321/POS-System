<?php

namespace App\Modules\PlatformCore\Application\DeviceAuth;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceEnrollmentCode;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class EnrollDevice
{
    public function __construct(
        private readonly DeviceTokenIssuer $tokenIssuer,
        private readonly ValidateAndroidEnrollmentAttestation $validateAndroidEnrollmentAttestation,
    ) {}

    /**
     * @param  array<string, mixed>|null  $attestationPayload
     * @return array<string, mixed>
     */
    public function handle(
        string $enrollmentCode,
        string $deviceName,
        string $platform,
        string $deviceFingerprint,
        string $publicKey,
        ?array $attestationPayload = null,
    ): array {
        $this->validateAndroidEnrollmentAttestation->handle(
            $platform,
            $publicKey,
            $attestationPayload,
        );

        $code = DeviceEnrollmentCode::query()
            ->where('code_hash', hash('sha256', Str::upper(trim($enrollmentCode))))
            ->whereNull('redeemed_at')
            ->where('expires_at', '>', CarbonImmutable::now('UTC'))
            ->first();

        if ($code === null) {
            throw new DeviceAuthException(
                'The enrollment code is invalid or has expired.',
                422,
                'ENROLLMENT_CODE_INVALID',
            );
        }

        if (
            Device::query()
                ->where('store_id', $code->store_id)
                ->where('name', $deviceName)
                ->exists()
        ) {
            throw new DeviceAuthException(
                'A device with this name already exists in the store.',
                422,
                'DEVICE_NAME_CONFLICT',
            );
        }

        /** @var array{device: Device, tokens: array<string, mixed>} $result */
        $result = DB::transaction(function () use ($attestationPayload, $code, $deviceFingerprint, $deviceName, $platform, $publicKey): array {
            $now = CarbonImmutable::now('UTC');
            $device = Device::query()->create([
                'merchant_id' => $code->merchant_id,
                'store_id' => $code->store_id,
                'device_profile_id' => $code->device_profile_id,
                'name' => $deviceName,
                'platform' => $platform,
                'status' => 'active',
                'device_fingerprint' => $deviceFingerprint,
                'public_key' => $publicKey,
                'attestation_payload' => $attestationPayload,
                'enrolled_at' => $now,
                'last_authenticated_at' => $now,
                'last_seen_at' => $now,
            ]);

            $code->forceFill([
                'redeemed_at' => $now,
                'redeemed_device_id' => $device->id,
            ])->save();

            return [
                'device' => $device,
                'tokens' => $this->tokenIssuer->issue(
                    $device,
                    (string) Str::ulid(),
                    $deviceFingerprint,
                ),
            ];
        });

        return [
            'device' => [
                'id' => $result['device']->id,
                'merchant_id' => $result['device']->merchant_id,
                'store_id' => $result['device']->store_id,
                'name' => $result['device']->name,
                'platform' => $result['device']->platform,
                'status' => $result['device']->status,
            ],
            'auth' => [
                ...$result['tokens'],
                'silent_refresh_window_minutes' => (int) config('pos.auth.silent_refresh_window_minutes', 5),
            ],
        ];
    }
}
