<?php

namespace App\Modules\OfflineSync\Application\Actions;

use App\Modules\OfflineSync\Application\Jobs\ReplaySyncEvent;
use App\Modules\OfflineSync\Application\Queries\DescribeSyncRecoveryRun;
use App\Modules\OfflineSync\Domain\Models\SyncEvent;
use App\Modules\OfflineSync\Domain\Models\SyncRecoveryRun;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;
use Illuminate\Bus\Batch;
use Illuminate\Support\Facades\Bus;
use Throwable;

class StartSyncRecoveryRun
{
    public function __construct(
        private readonly DescribeSyncRecoveryRun $describeSyncRecoveryRun,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device): array
    {
        $events = SyncEvent::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('device_id', $device->id)
            ->whereIn('status', ['error', 'manual_review'])
            ->orderBy('received_at')
            ->limit((int) config('pos.sync.recovery_batch_size', 50))
            ->get();

        $run = SyncRecoveryRun::query()->create([
            'merchant_id' => $device->merchant_id,
            'store_id' => $device->store_id,
            'initiated_by_device_id' => $device->id,
            'status' => $events->isEmpty() ? 'completed' : 'queued',
            'event_count' => $events->count(),
            'started_at' => CarbonImmutable::now('UTC'),
            'finished_at' => $events->isEmpty() ? CarbonImmutable::now('UTC') : null,
        ]);

        if ($events->isEmpty()) {
            return $this->describeSyncRecoveryRun->handle($device, $run);
        }

        $runId = $run->id;
        $eventIds = $events->map(fn (SyncEvent $event): string => $event->id)->all();

        SyncEvent::query()
            ->whereIn('id', $eventIds)
            ->update([
                'status' => 'recovering',
            ]);

        $batch = Bus::batch(
            $events->map(fn (SyncEvent $event): ReplaySyncEvent => new ReplaySyncEvent($event->id, $runId))->all(),
        )->name('sync-recovery-'.$runId)
            ->onConnection((string) config('pos.sync.recovery_connection', config('queue.default')))
            ->onQueue((string) config('pos.sync.recovery_queue', 'sync'))
            ->then(function (Batch $batch) use ($runId): void {
                SyncRecoveryRun::query()
                    ->whereKey($runId)
                    ->update([
                        'status' => 'completed',
                        'finished_at' => CarbonImmutable::now('UTC'),
                    ]);
            })
            ->catch(function (Batch $batch, Throwable $throwable) use ($runId): void {
                SyncRecoveryRun::query()
                    ->whereKey($runId)
                    ->update([
                        'status' => 'failed',
                        'finished_at' => CarbonImmutable::now('UTC'),
                    ]);
            })
            ->dispatch();

        SyncRecoveryRun::query()->whereKey($run->id)->update([
            'batch_id' => $batch->id,
        ]);
        SyncRecoveryRun::query()
            ->whereKey($run->id)
            ->where('status', 'queued')
            ->update([
                'status' => 'running',
            ]);

        SyncEvent::query()
            ->whereIn('id', $eventIds)
            ->update([
                'recovery_batch_id' => $batch->id,
            ]);

        /** @var SyncRecoveryRun $freshRun */
        $freshRun = SyncRecoveryRun::query()->findOrFail($run->id);

        return $this->describeSyncRecoveryRun->handle($device, $freshRun);
    }
}
