<?php

namespace App\Modules\PlatformCore\Application\Bootstrap;

use App\Modules\OfflineSync\Domain\Models\SyncEvent;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Application\Features\FeatureFlagSnapshotQuery;
use App\Modules\PlatformCore\Domain\Models\Device;

class PosBootstrapService
{
    public function __construct(
        private readonly FeatureFlagSnapshotQuery $featureFlagSnapshotQuery,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function build(Device $device): array
    {
        $device->loadMissing('merchant', 'store', 'profile');
        $activeRegisterSession = RegisterSession::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('device_id', $device->id)
            ->where('status', 'open')
            ->latest('opened_at')
            ->first();

        return [
            'support' => [
                'api_major' => config('pos.api.current_major'),
                'supported_majors' => config('pos.api.supported_majors'),
                'min_supported_major' => config('pos.api.min_supported_major'),
                'min_supported_app_version' => config('pos.api.min_supported_app_version'),
                'sunset_at' => config('pos.api.sunset_at'),
                'upgrade_required' => false,
            ],
            'merchant' => [
                'id' => $device->merchant?->id,
                'name' => $device->merchant?->name,
                'currency' => $device->merchant?->currency,
            ],
            'store' => [
                'id' => $device->store?->id,
                'name' => $device->store?->name,
                'code' => $device->store?->code,
                'mode' => $device->store?->mode,
                'timezone' => $device->store?->timezone,
                'business_day_cutoff' => $device->store?->business_day_cutoff,
            ],
            'device' => [
                'id' => $device->id,
                'name' => $device->name,
                'status' => $device->status,
                'platform' => $device->platform,
                'profile' => [
                    'id' => $device->profile?->id,
                    'name' => $device->profile?->name,
                    'type' => $device->profile?->type,
                    'capabilities' => $device->profile?->capabilities ?? [],
                ],
            ],
            'register' => [
                'active_session' => $activeRegisterSession === null ? null : [
                    'id' => $activeRegisterSession->id,
                    'status' => $activeRegisterSession->status,
                    'business_date' => $activeRegisterSession->business_date?->format('Y-m-d'),
                    'session_version' => $activeRegisterSession->session_version,
                    'opening_float_minor' => $activeRegisterSession->opening_float_minor,
                    'expected_cash_minor' => $activeRegisterSession->expected_cash_minor,
                ],
            ],
            'feature_flags' => $this->featureFlagSnapshotQuery->forDevice($device),
            'sync' => [
                'queued_events' => 0,
                'last_server_cursor' => SyncEvent::query()
                    ->where('merchant_id', $device->merchant_id)
                    ->where('store_id', $device->store_id)
                    ->max('received_at'),
            ],
        ];
    }
}
