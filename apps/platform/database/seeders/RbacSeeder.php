<?php

namespace Database\Seeders;

use App\Modules\Identity\Application\ProvisionMerchantRoles;
use Illuminate\Database\Seeder;

class RbacSeeder extends Seeder
{
    public function run(): void
    {
        app(ProvisionMerchantRoles::class)->ensurePermissions();
    }
}
