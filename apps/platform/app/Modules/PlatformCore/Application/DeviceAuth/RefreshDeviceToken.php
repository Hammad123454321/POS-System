<?php

namespace App\Modules\PlatformCore\Application\DeviceAuth;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceRefreshToken;
use Carbon\CarbonImmutable;

class RefreshDeviceToken
{
    public function __construct(
        private readonly DeviceTokenIssuer $tokenIssuer,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(string $refreshToken): array
    {
        $hashedToken = hash('sha256', $refreshToken);
        $storedToken = DeviceRefreshToken::query()
            ->where('token_hash', $hashedToken)
            ->first();

        if ($storedToken === null) {
            throw new DeviceAuthException(
                'The refresh token is invalid.',
                401,
                'INVALID_REFRESH_TOKEN',
            );
        }

        if ($storedToken->revoked_at !== null) {
            $this->tokenIssuer->revokeFamily($storedToken, 'reuse_detected');

            throw new DeviceAuthException(
                'Refresh token reuse detected. Device re-authentication is required.',
                401,
                'DEVICE_REAUTH_REQUIRED',
            );
        }

        $device = $storedToken->device()->with('store.merchant')->first();

        if (! $device instanceof Device) {
            throw new DeviceAuthException(
                'The device session is no longer valid.',
                401,
                'DEVICE_REAUTH_REQUIRED',
            );
        }

        if (
            $storedToken->expires_at?->isPast()
            || $device->status !== 'active'
            || $device->store?->status !== 'active'
            || $device->store?->merchant?->status !== 'active'
        ) {
            $this->tokenIssuer->revokeFamily($storedToken, 'device_inactive');

            throw new DeviceAuthException(
                'The device session requires re-authentication.',
                401,
                'DEVICE_REAUTH_REQUIRED',
            );
        }

        $now = CarbonImmutable::now('UTC');
        $storedToken->forceFill([
            'last_used_at' => $now,
            'revoked_at' => $now,
            'revoked_reason' => 'rotated',
        ])->save();

        $device->forceFill([
            'last_authenticated_at' => $now,
            'last_seen_at' => $now,
        ])->save();

        return [
            'device' => [
                'id' => $device->id,
                'status' => $device->status,
            ],
            'auth' => [
                ...$this->tokenIssuer->issue(
                    $device,
                    $storedToken->token_family_id,
                    $storedToken->device_fingerprint,
                    $storedToken->id,
                ),
                'silent_refresh_window_minutes' => (int) config('pos.auth.silent_refresh_window_minutes', 5),
            ],
        ];
    }
}
