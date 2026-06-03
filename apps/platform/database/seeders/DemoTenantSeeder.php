<?php

namespace Database\Seeders;

use App\Modules\PlatformCore\Domain\Models\DeviceEnrollmentCode;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Database\Seeder;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class DemoTenantSeeder extends Seeder
{
    public function run(): void
    {
        $merchant = Merchant::query()->where('name', 'Demo Café')->first();
        if (! $merchant) {
            $merchant = Merchant::query()->create([
                'name' => 'Demo Café',
                'currency' => 'USD',
                'status' => 'active',
            ]);
        }

        DB::statement("SELECT set_config('app.current_merchant_id', ?, true)", [$merchant->id]);

        $store = Store::query()
            ->where('merchant_id', $merchant->id)
            ->where('code', 'MAIN')
            ->first();
        if (! $store) {
            $store = Store::query()->create([
                'merchant_id' => $merchant->id,
                'name' => 'Demo Café — Main',
                'code' => 'MAIN',
                'mode' => 'restaurant',
                'timezone' => 'America/New_York',
                'business_day_cutoff' => '04:00',
                'status' => 'active',
            ]);
        }

        $profile = DeviceProfile::query()->where('name', 'Front-of-House Tablet')->first();
        if (! $profile) {
            $profile = DeviceProfile::query()->create([
                'name' => 'Front-of-House Tablet',
                'type' => 'tablet',
                'capabilities' => [
                    'card' => true,
                    'receipt_printer' => true,
                    'kitchen_printer' => true,
                    'scanner' => false,
                ],
            ]);
        }

        $plainCode = strtoupper(Str::random(6).'-'.Str::random(6).'-'.Str::random(6));
        $code = DeviceEnrollmentCode::query()->create([
            'merchant_id' => $merchant->id,
            'store_id' => $store->id,
            'device_profile_id' => $profile->id,
            'code_hash' => hash('sha256', $plainCode),
            'expires_at' => Carbon::now()->addMinutes(15),
        ]);

        $this->command->info('---');
        $this->command->info('Merchant ID: '.$merchant->id);
        $this->command->info('Store ID:    '.$store->id);
        $this->command->info('Profile ID:  '.$profile->id);
        $this->command->info('Enrollment code (one-time, 15 min): '.$plainCode);
        $this->command->info('---');
    }
}
