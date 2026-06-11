<?php

namespace App\Console\Commands;

use App\Modules\Identity\Application\ProvisionMerchantRoles;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use Illuminate\Console\Command;

class ProvisionMerchantRolesCommand extends Command
{
    protected $signature = 'app:provision-merchant-roles {merchant? : Merchant ULID; omit to provision all merchants}';

    protected $description = 'Ensure canonical roles and permissions exist for one or all merchants.';

    public function handle(ProvisionMerchantRoles $provision): int
    {
        $merchantId = $this->argument('merchant');

        $merchants = $merchantId !== null
            ? Merchant::query()->where('id', $merchantId)->get()
            : Merchant::query()->get();

        if ($merchants->isEmpty()) {
            $this->error($merchantId !== null
                ? "No merchant found for [{$merchantId}]."
                : 'No merchants exist yet.');

            return self::FAILURE;
        }

        foreach ($merchants as $merchant) {
            $provision->handle($merchant);
            $this->info("Provisioned roles for [{$merchant->name}] ({$merchant->id}).");
        }

        return self::SUCCESS;
    }
}
