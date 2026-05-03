<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Models\User;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Support\WorkforceAuthorization;
use App\Modules\SalonWorkforce\Contracts\PayrollCalculator;
use Carbon\CarbonImmutable;
use Illuminate\Auth\Access\AuthorizationException;

class GenerateWeeklyPayrollSnapshot
{
    public function __construct(
        private readonly WorkforceAuthorization $authorization,
        private readonly PayrollCalculator $payrollCalculator,
        private readonly AuditLogger $auditLogger,
        private readonly RecordUsage $recordUsage,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        ?string $weekReferenceDate = null,
    ): array {
        if (! $this->authorization->canManageStore($actor, $store)) {
            throw new AuthorizationException('You are not allowed to generate payroll snapshots for this store.');
        }

        $reference = $weekReferenceDate === null
            ? CarbonImmutable::now($store->timezone)
            : CarbonImmutable::parse($weekReferenceDate, $store->timezone);
        $periodStart = $reference->startOfWeek(CarbonImmutable::MONDAY);
        $periodEnd = $periodStart->addDays(6);

        $snapshot = $this->payrollCalculator->generatePayrollReport(
            $store,
            $periodStart,
            $periodEnd,
            $actor->id,
        );

        $this->auditLogger->log(
            $store->merchant_id,
            $store->id,
            'salon_workforce',
            'payroll_snapshot.generated',
            'payroll_snapshot',
            $snapshot->id,
            null,
            [
                'period_type' => $snapshot->period_type,
                'period_start' => $snapshot->period_start?->format('Y-m-d'),
                'period_end' => $snapshot->period_end?->format('Y-m-d'),
                'gross_pay_minor' => $snapshot->gross_pay_minor,
            ],
            null,
            $actor->id,
        );
        $this->recordUsage->handle($store->merchant_id, $store->id, 'salon.payroll_snapshot.generated');

        return [
            'id' => $snapshot->id,
            'period_type' => $snapshot->period_type,
            'period_start' => $snapshot->period_start?->format('Y-m-d'),
            'period_end' => $snapshot->period_end?->format('Y-m-d'),
            'staff_count' => $snapshot->staff_count,
            'approved_minutes' => $snapshot->approved_minutes,
            'gross_wages_minor' => $snapshot->gross_wages_minor,
            'gross_commission_minor' => $snapshot->gross_commission_minor,
            'gross_pay_minor' => $snapshot->gross_pay_minor,
            'payload' => $snapshot->payload ?? [],
            'generated_at' => $snapshot->generated_at?->toIso8601String(),
        ];
    }
}
