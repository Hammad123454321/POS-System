<?php

namespace App\Modules\PlatformCore\Application\Onboarding;

use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;

class CreateStore
{
    public function handle(
        Merchant $merchant,
        string $name,
        ?string $code,
        string $mode,
        string $timezone,
        string $businessDayCutoff,
    ): Store {
        return Store::query()->create([
            'merchant_id' => $merchant->id,
            'name' => $name,
            'code' => $code,
            'mode' => $mode,
            'timezone' => $timezone,
            'business_day_cutoff' => $businessDayCutoff,
            'status' => 'active',
        ]);
    }
}
