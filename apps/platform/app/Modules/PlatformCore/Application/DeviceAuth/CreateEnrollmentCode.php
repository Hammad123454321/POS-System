<?php

namespace App\Modules\PlatformCore\Application\DeviceAuth;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\DeviceEnrollmentCode;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Store;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\Gate;
use Illuminate\Support\Str;

class CreateEnrollmentCode
{
    /**
     * @return array<string, mixed>
     */
    public function handle(User $actor, Store $store, string $deviceProfileId): array
    {
        Gate::forUser($actor)->authorize('manageDevices', $store);

        $profile = DeviceProfile::query()->findOrFail($deviceProfileId);
        $plainCode = $this->generatePlainCode();
        $expiresAt = CarbonImmutable::now('UTC')
            ->addMinutes((int) config('pos.auth.enrollment_ttl_minutes', 15));

        DeviceEnrollmentCode::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'device_profile_id' => $profile->id,
            'created_by_user_id' => $actor->id,
            'code_hash' => hash('sha256', $plainCode),
            'expires_at' => $expiresAt,
        ]);

        return [
            'code' => $plainCode,
            'expires_at' => $expiresAt->toIso8601String(),
            'store_id' => $store->id,
            'device_profile_id' => $profile->id,
        ];
    }

    private function generatePlainCode(): string
    {
        return Str::upper(Str::random(6).'-'.Str::random(6).'-'.Str::random(6));
    }
}
