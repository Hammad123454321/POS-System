<?php

namespace App\Modules\PlatformCore\Application\Actions;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\DeviceStatusEvent;
use Carbon\CarbonImmutable;

class CreateDeviceStatusEvent
{
    /**
     * @param  array<string, mixed>|null  $metadata
     */
    public function handle(
        Device $device,
        string $status,
        ?array $metadata = null,
        ?string $occurredAt = null,
    ): DeviceStatusEvent {
        return DeviceStatusEvent::query()->create([
            'merchant_id' => $device->merchant_id,
            'store_id' => $device->store_id,
            'device_id' => $device->id,
            'status' => $status,
            'metadata' => $metadata,
            'occurred_at' => $occurredAt ? CarbonImmutable::parse($occurredAt, 'UTC') : CarbonImmutable::now('UTC'),
        ]);
    }
}
