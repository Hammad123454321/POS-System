<?php

namespace App\Modules\PlatformCore\Application\Devices;

use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Time\BusinessClock;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;

/**
 * Resolves a per-store "system" device + open register session used by
 * server-side flows (e.g. delivery webhook ingestion) that have no physical
 * device or cashier. Idempotent: find-or-create.
 */
class ResolveSystemDevice
{
    public const DEVICE_NAME = '_system_delivery';

    public const PROFILE_NAME = 'Server Agent';

    public function __construct(
        private readonly BusinessClock $businessClock,
    ) {}

    public function handle(Store $store): Device
    {
        return DB::transaction(function () use ($store): Device {
            $profile = DeviceProfile::query()->firstOrCreate(
                ['name' => self::PROFILE_NAME],
                ['type' => 'server', 'capabilities' => ['system' => true]],
            );

            $device = Device::query()->firstOrCreate(
                ['store_id' => $store->id, 'name' => self::DEVICE_NAME],
                [
                    'merchant_id' => $store->merchant_id,
                    'device_profile_id' => $profile->id,
                    'platform' => 'server',
                    'status' => 'active',
                    'drawer_code' => '_delivery',
                ],
            );

            $this->ensureOpenSession($store, $device);

            return $device;
        });
    }

    private function ensureOpenSession(Store $store, Device $device): void
    {
        $businessDate = $this->businessClock->businessDateForStore($store, CarbonImmutable::now('UTC'));

        $existing = RegisterSession::query()
            ->where('device_id', $device->id)
            ->where('status', 'open')
            ->where('business_date', $businessDate)
            ->first();

        if ($existing !== null) {
            return;
        }

        RegisterSession::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'device_id' => $device->id,
            'drawer_code' => '_delivery',
            'business_date' => $businessDate,
            'status' => 'open',
            'session_version' => 1,
            'opening_float_minor' => 0,
            'expected_cash_minor' => 0,
            'opened_at' => CarbonImmutable::now('UTC'),
        ]);
    }
}
