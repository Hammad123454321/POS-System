<?php

namespace App\Modules\PlatformCore\Application\Onboarding;

use App\Modules\PlatformCore\Domain\Models\DeviceProfile;

class CreateDeviceProfile
{
    /**
     * @param  array<string, mixed>  $capabilities
     */
    public function handle(string $name, string $type, array $capabilities = []): DeviceProfile
    {
        return DeviceProfile::query()->create([
            'name' => $name,
            'type' => $type,
            'capabilities' => $capabilities,
        ]);
    }
}
