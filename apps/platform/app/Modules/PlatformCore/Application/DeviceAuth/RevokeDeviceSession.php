<?php

namespace App\Modules\PlatformCore\Application\DeviceAuth;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceRefreshToken;
use Carbon\CarbonImmutable;
use Illuminate\Http\Request;

class RevokeDeviceSession
{
    public function handle(Request $request, Device $device): void
    {
        $now = CarbonImmutable::now('UTC');

        $device->tokens()->delete();

        DeviceRefreshToken::query()
            ->where('device_id', $device->id)
            ->whereNull('revoked_at')
            ->update([
                'revoked_at' => $now,
                'revoked_reason' => 'logout',
            ]);

        $request->user()?->currentAccessToken()?->delete();
    }
}
