<?php

namespace App\Modules\SalonWorkforce\Domain\Events;

/**
 * Emitted (after commit) when a payroll snapshot is generated. Carries IDs only —
 * listeners are queued and must not serialize Eloquent models.
 */
class PayrollSnapshotGenerated
{
    public function __construct(
        public readonly string $payrollSnapshotId,
        public readonly ?string $merchantId,
        public readonly ?string $storeId,
    ) {}
}
