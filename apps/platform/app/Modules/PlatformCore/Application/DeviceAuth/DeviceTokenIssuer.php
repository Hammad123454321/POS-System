<?php

namespace App\Modules\PlatformCore\Application\DeviceAuth;

use App\Modules\OfflineSync\Domain\Models\OutboxJob;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceRefreshToken;
use Carbon\CarbonImmutable;
use Illuminate\Support\Str;

class DeviceTokenIssuer
{
    /**
     * @return array<string, mixed>
     */
    public function issue(Device $device, string $tokenFamilyId, ?string $deviceFingerprint = null, ?string $rotatedFromId = null): array
    {
        $accessToken = $device->createToken(
            'pos-access',
            ['pos:access'],
            CarbonImmutable::now('UTC')->addMinutes((int) config('pos.auth.access_token_ttl_minutes', 15)),
        );

        $plainRefreshToken = Str::random(96);
        $refreshTokenExpiresAt = CarbonImmutable::now('UTC')
            ->addDays((int) config('pos.auth.refresh_token_ttl_days', 30));

        DeviceRefreshToken::query()->create([
            'merchant_id' => $device->merchant_id,
            'store_id' => $device->store_id,
            'device_id' => $device->id,
            'token_family_id' => $tokenFamilyId,
            'rotated_from_id' => $rotatedFromId,
            'token_hash' => hash('sha256', $plainRefreshToken),
            'device_fingerprint' => $deviceFingerprint,
            'expires_at' => $refreshTokenExpiresAt,
        ]);

        return [
            'access_token' => $accessToken->plainTextToken,
            'access_token_expires_at' => $accessToken->accessToken->expires_at?->toIso8601String(),
            'refresh_token' => $plainRefreshToken,
            'refresh_token_expires_at' => $refreshTokenExpiresAt->toIso8601String(),
            'token_family_id' => $tokenFamilyId,
        ];
    }

    public function revokeFamily(DeviceRefreshToken $refreshToken, string $reason): void
    {
        DeviceRefreshToken::query()
            ->where('device_id', $refreshToken->device_id)
            ->where('token_family_id', $refreshToken->token_family_id)
            ->whereNull('revoked_at')
            ->update([
                'revoked_at' => CarbonImmutable::now('UTC'),
                'revoked_reason' => $reason,
            ]);

        $device = $refreshToken->device()->first();

        if ($device instanceof Device) {
            $device->tokens()->delete();

            OutboxJob::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'channel' => 'security',
                'payload' => [
                    'type' => 'device_token_family_revoked',
                    'device_id' => $device->id,
                    'token_family_id' => $refreshToken->token_family_id,
                    'reason' => $reason,
                ],
                'available_at' => CarbonImmutable::now('UTC'),
            ]);
        }
    }
}
