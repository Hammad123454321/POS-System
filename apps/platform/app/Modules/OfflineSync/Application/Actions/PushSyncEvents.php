<?php

namespace App\Modules\OfflineSync\Application\Actions;

use App\Modules\OfflineSync\Application\Arbitration\ArbitrateSyncEvent;
use App\Modules\OfflineSync\Domain\Models\SyncEvent;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;

class PushSyncEvents
{
    public function __construct(
        private readonly ArbitrateSyncEvent $arbitrate,
    ) {}

    /**
     * @param  array<int, array{local_event_id: string, entity_type: string, entity_id?: string|null, action: string, payload: array<string, mixed>}>  $events
     * @return array<int, array<string, mixed>>
     */
    public function handle(Device $device, array $events): array
    {
        $accepted = [];

        foreach ($events as $event) {
            $existing = SyncEvent::query()
                ->where('device_id', $device->id)
                ->where('local_event_id', $event['local_event_id'])
                ->first();

            if ($existing !== null) {
                $accepted[] = [
                    'local_event_id' => $existing->local_event_id,
                    'status' => 'duplicate',
                    'sync_event_id' => $existing->id,
                ];

                continue;
            }

            $verdict = $this->arbitrate->handle($device, $event);

            $syncEvent = SyncEvent::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'device_id' => $device->id,
                'local_event_id' => $event['local_event_id'],
                'entity_type' => $event['entity_type'],
                'entity_id' => $event['entity_id'] ?? null,
                'action' => $event['action'],
                'payload' => $event['payload'],
                'status' => $verdict['status'],
                'conflict_code' => $verdict['conflict_code'],
                'received_at' => CarbonImmutable::now('UTC'),
                'processed_at' => CarbonImmutable::now('UTC'),
            ]);

            $accepted[] = [
                'local_event_id' => $syncEvent->local_event_id,
                'status' => $syncEvent->status,
                'conflict_code' => $verdict['conflict_code'],
                'server_status_seq' => $verdict['server_status_seq'] ?? null,
                'sync_event_id' => $syncEvent->id,
            ];
        }

        return $accepted;
    }
}
