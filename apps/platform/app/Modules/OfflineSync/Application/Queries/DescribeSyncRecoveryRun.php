<?php

namespace App\Modules\OfflineSync\Application\Queries;

use App\Modules\OfflineSync\Domain\Models\SyncRecoveryRun;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Support\Facades\Bus;

class DescribeSyncRecoveryRun
{
    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, SyncRecoveryRun $run): array
    {
        if ($run->merchant_id !== $device->merchant_id || $run->store_id !== $device->store_id) {
            throw new DomainException('The requested sync recovery run does not belong to this device context.');
        }

        $batch = $run->batch_id === null ? null : Bus::findBatch($run->batch_id);

        return [
            'id' => $run->id,
            'status' => $run->status,
            'event_count' => $run->event_count,
            'batch_id' => $run->batch_id,
            'started_at' => $run->started_at?->toIso8601String(),
            'finished_at' => $run->finished_at?->toIso8601String(),
            'batch' => $batch === null ? null : [
                'id' => $batch->id,
                'name' => $batch->name,
                'total_jobs' => $batch->totalJobs,
                'pending_jobs' => $batch->pendingJobs,
                'failed_jobs' => $batch->failedJobs,
                'processed_jobs' => $batch->processedJobs(),
                'progress' => $batch->progress(),
                'cancelled' => $batch->cancelled(),
                'finished' => $batch->finished(),
            ],
        ];
    }
}
