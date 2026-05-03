<?php

namespace App\Modules\SalonWorkforce\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Domain\Models\PayrollSnapshot;
use DomainException;

class ShowPayrollSnapshot
{
    /**
     * @return array<string, mixed>
     */
    public function handle(Store $store, PayrollSnapshot $snapshot): array
    {
        if ($snapshot->merchant_id !== $store->merchant_id || $snapshot->store_id !== $store->id) {
            throw new DomainException('Payroll snapshot does not belong to this store.');
        }

        return [
            'id' => $snapshot->id,
            'period_type' => $snapshot->period_type,
            'period_start' => $snapshot->period_start?->format('Y-m-d'),
            'period_end' => $snapshot->period_end?->format('Y-m-d'),
            'staff_count' => $snapshot->staff_count,
            'approved_minutes' => $snapshot->approved_minutes,
            'regular_minutes' => $snapshot->regular_minutes,
            'overtime_minutes' => $snapshot->overtime_minutes,
            'gross_wages_minor' => $snapshot->gross_wages_minor,
            'gross_commission_minor' => $snapshot->gross_commission_minor,
            'gross_pay_minor' => $snapshot->gross_pay_minor,
            'payload' => $snapshot->payload ?? [],
            'generated_at' => $snapshot->generated_at?->toIso8601String(),
        ];
    }
}
