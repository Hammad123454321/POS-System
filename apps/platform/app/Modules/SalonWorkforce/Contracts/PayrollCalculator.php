<?php

namespace App\Modules\SalonWorkforce\Contracts;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Domain\Models\PayrollSnapshot;
use Carbon\CarbonImmutable;

interface PayrollCalculator
{
    /**
     * @return array<string, int>
     */
    public function snapshotApprovedHours(
        Store $store,
        CarbonImmutable $periodStart,
        CarbonImmutable $periodEnd,
    ): array;

    public function calculateGross(int $approvedMinutes, int $hourlyRateMinor): int;

    public function calculateCommission(
        int $servicePriceMinor,
        int $discountMinor,
        string $baseType,
        ?int $rateBasisPoints,
        ?int $fixedMinor,
        string $currency,
    ): int;

    public function generatePayrollReport(
        Store $store,
        CarbonImmutable $periodStart,
        CarbonImmutable $periodEnd,
        ?int $generatedByUserId = null,
    ): PayrollSnapshot;
}
