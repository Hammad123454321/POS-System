<?php

namespace App\Modules\Billing\Application\Listeners;

use App\Modules\Billing\Application\RecordUsage;
use App\Modules\SalonWorkforce\Domain\Events\PayrollSnapshotGenerated;
use Illuminate\Contracts\Queue\ShouldQueue;

class RecordPayrollSnapshotUsage implements ShouldQueue
{
    public string $queue = 'reporting';

    public function __construct(
        private readonly RecordUsage $recordUsage,
    ) {}

    public function handle(PayrollSnapshotGenerated $event): void
    {
        $this->recordUsage->handle(
            merchantId: $event->merchantId,
            storeId: $event->storeId,
            metricKey: 'payroll.snapshots.generated',
            quantity: 1,
            metadata: ['payroll_snapshot_id' => $event->payrollSnapshotId],
            sourceRef: $event->payrollSnapshotId,
        );
    }
}
