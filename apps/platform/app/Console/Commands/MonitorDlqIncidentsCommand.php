<?php

namespace App\Console\Commands;

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use Illuminate\Console\Command;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class MonitorDlqIncidentsCommand extends Command
{
    protected $signature = 'pos:dlq-monitor';

    protected $description = 'Open critical exception cases for stale failed queue jobs.';

    public function handle(): int
    {
        $cutoff = Carbon::now('UTC')->subMinutes((int) config('pos.incidents.dlq_stale_minutes', 15));
        $failedJobs = DB::table('failed_jobs')
            ->where('failed_at', '<=', $cutoff)
            ->orderBy('failed_at')
            ->limit(100)
            ->get();

        $opened = 0;

        foreach ($failedJobs as $job) {
            $jobUuid = (string) ($job->uuid ?? $job->id);
            $alreadyOpen = ExceptionCase::query()
                ->where('type', 'dlq_stale_job')
                ->where('record_type', 'failed_job')
                ->where('record_id', $jobUuid)
                ->where('status', 'open')
                ->exists();

            if ($alreadyOpen) {
                continue;
            }

            ExceptionCase::query()->create([
                'merchant_id' => null,
                'store_id' => null,
                'type' => 'dlq_stale_job',
                'module' => $this->moduleFromQueue((string) $job->queue),
                'severity' => 'critical',
                'status' => 'open',
                'message' => 'Failed queue job has exceeded the DLQ age threshold.',
                'record_type' => 'failed_job',
                'record_id' => $jobUuid,
                'details' => [
                    'connection' => $job->connection,
                    'queue' => $job->queue,
                    'failed_at' => $job->failed_at,
                    'exception_excerpt' => Str::limit((string) $job->exception, 1000),
                ],
            ]);

            $opened++;
        }

        $this->info("Opened {$opened} DLQ incident exception case(s).");

        return self::SUCCESS;
    }

    private function moduleFromQueue(string $queue): string
    {
        return match (true) {
            str_contains($queue, 'payment') => 'payments',
            str_contains($queue, 'sync') => 'sync',
            str_contains($queue, 'delivery') => 'delivery',
            str_contains($queue, 'reporting') => 'reporting',
            default => 'platform',
        };
    }
}
