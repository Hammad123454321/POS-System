<?php

namespace App\Modules\PlatformCore\Application\Onboarding;

use App\Modules\Identity\Application\ProvisionMerchantRoles;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use Illuminate\Support\Facades\DB;

class CreateMerchant
{
    public function __construct(
        private readonly ProvisionMerchantRoles $provisionMerchantRoles,
    ) {}

    public function handle(string $name, string $currency): Merchant
    {
        return DB::transaction(function () use ($name, $currency): Merchant {
            $merchant = Merchant::query()->create([
                'name' => $name,
                'currency' => strtoupper($currency),
                'status' => 'active',
            ]);

            $this->provisionMerchantRoles->handle($merchant);

            return $merchant;
        });
    }
}
