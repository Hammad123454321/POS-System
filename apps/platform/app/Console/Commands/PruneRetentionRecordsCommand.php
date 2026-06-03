<?php

namespace App\Console\Commands;

use App\Modules\Audit\Domain\Models\AuditLog;
use App\Modules\OfflineSync\Domain\Models\SyncEvent;
use App\Modules\OrderRegister\Domain\Models\Receipt;
use App\Modules\PlatformCore\Domain\Models\ArchiveAccessLog;
use App\Modules\PlatformCore\Domain\Models\DeviceStatusEvent;
use App\Modules\SalonWorkforce\Domain\Models\PayrollSnapshot;
use Illuminate\Console\Command;
use Illuminate\Support\Carbon;

class PruneRetentionRecordsCommand extends Command
{
    protected $signature = 'pos:retention-prune {--dry-run : Report counts without deleting records}';

    protected $description = 'Prune records governed by POS retention policy.';

    public function handle(): int
    {
        $dryRun = (bool) $this->option('dry-run');
        $targets = [
            'sync_events' => [
                'model' => SyncEvent::class,
                'column' => 'received_at',
                'days' => (int) config('pos.retention.sync_events_days', 180),
            ],
            'device_status_events' => [
                'model' => DeviceStatusEvent::class,
                'column' => 'occurred_at',
                'days' => (int) config('pos.retention.device_status_events_days', 365),
            ],
            'archive_access_logs' => [
                'model' => ArchiveAccessLog::class,
                'column' => 'accessed_at',
                'days' => (int) config('pos.retention.archive_access_logs_days', 2555),
            ],
            'audit_logs' => [
                'model' => AuditLog::class,
                'column' => 'occurred_at',
                'days' => (int) config('pos.retention.audit_logs_days', 2555),
            ],
            'payroll_snapshots' => [
                'model' => PayrollSnapshot::class,
                'column' => 'generated_at',
                'days' => (int) config('pos.retention.payroll_snapshots_days', 2555),
            ],
            'receipts' => [
                'model' => Receipt::class,
                'column' => 'printed_at',
                'days' => (int) config('pos.retention.receipts_days', 2555),
            ],
        ];

        foreach ($targets as $name => $target) {
            $cutoff = Carbon::now('UTC')->subDays($target['days']);
            $query = $target['model']::query()->whereNotNull($target['column'])->where($target['column'], '<', $cutoff);
            $count = (clone $query)->count();

            if (! $dryRun && $count > 0) {
                $query->delete();
            }

            $this->info(sprintf('%s: %d %s', $name, $count, $dryRun ? 'eligible' : 'pruned'));
        }

        return self::SUCCESS;
    }
}
