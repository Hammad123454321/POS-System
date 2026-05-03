<?php

namespace App\Modules\SalonWorkforce\Application;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Contracts\PayrollCalculator;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use App\Modules\SalonWorkforce\Domain\Models\AttendanceRecord;
use App\Modules\SalonWorkforce\Domain\Models\PayrollSnapshot;
use App\Modules\SalonWorkforce\Domain\Models\StaffServiceRule;
use App\Modules\SalonWorkforce\Domain\Models\WageRule;
use App\Platform\Support\Money\MinorAmount;
use Brick\Math\RoundingMode;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class EloquentPayrollCalculator implements PayrollCalculator
{
    public function snapshotApprovedHours(
        Store $store,
        CarbonImmutable $periodStart,
        CarbonImmutable $periodEnd,
    ): array {
        return AttendanceRecord::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->where('approval_status', 'approved')
            ->whereBetween('business_date', [
                $periodStart->toDateString(),
                $periodEnd->toDateString(),
            ])
            ->selectRaw('staff_profile_id, SUM(worked_minutes) AS approved_minutes')
            ->groupBy('staff_profile_id')
            ->pluck('approved_minutes', 'staff_profile_id')
            ->map(static fn ($minutes): int => (int) $minutes)
            ->all();
    }

    public function calculateGross(int $approvedMinutes, int $hourlyRateMinor): int
    {
        if ($approvedMinutes <= 0 || $hourlyRateMinor <= 0) {
            return 0;
        }

        return intdiv(($approvedMinutes * $hourlyRateMinor) + 30, 60);
    }

    public function calculateCommission(
        int $servicePriceMinor,
        int $discountMinor,
        string $baseType,
        ?int $rateBasisPoints,
        ?int $fixedMinor,
        string $currency,
    ): int {
        $baseMinor = match ($baseType) {
            'service_subtotal' => max(0, $servicePriceMinor),
            'service_total' => max(0, $servicePriceMinor),
            default => max(0, $servicePriceMinor - max(0, $discountMinor)),
        };

        if (($fixedMinor ?? 0) > 0) {
            return (int) $fixedMinor;
        }

        $basisPoints = (int) ($rateBasisPoints ?? 0);

        if ($basisPoints <= 0 || $baseMinor <= 0) {
            return 0;
        }

        return MinorAmount::percentageOf($baseMinor, $basisPoints, $currency, RoundingMode::HALF_UP);
    }

    public function generatePayrollReport(
        Store $store,
        CarbonImmutable $periodStart,
        CarbonImmutable $periodEnd,
        ?int $generatedByUserId = null,
    ): PayrollSnapshot {
        return DB::transaction(function () use ($generatedByUserId, $periodEnd, $periodStart, $store): PayrollSnapshot {
            $approvedHoursByStaff = $this->snapshotApprovedHours($store, $periodStart, $periodEnd);

            $appointments = Appointment::query()
                ->where('merchant_id', $store->merchant_id)
                ->where('store_id', $store->id)
                ->where('status', 'completed')
                ->whereBetween('business_date', [
                    $periodStart->toDateString(),
                    $periodEnd->toDateString(),
                ])
                ->orderBy('staff_profile_id')
                ->get();

            $staffServiceRules = StaffServiceRule::query()
                ->where('merchant_id', $store->merchant_id)
                ->where('store_id', $store->id)
                ->where('is_active', true)
                ->with('commissionRule')
                ->get()
                ->keyBy(fn (StaffServiceRule $rule): string => "{$rule->staff_profile_id}:{$rule->service_item_id}");

            $commissionByStaff = [];

            foreach ($appointments as $appointment) {
                $ruleKey = "{$appointment->staff_profile_id}:{$appointment->service_item_id}";
                /** @var StaffServiceRule|null $assignment */
                $assignment = $staffServiceRules->get($ruleKey);
                $commissionRule = $assignment?->commissionRule;

                if ($commissionRule === null || ! $commissionRule->is_active) {
                    continue;
                }

                $commissionMinor = $this->calculateCommission(
                    $appointment->service_price_minor,
                    $appointment->discount_minor,
                    (string) $commissionRule->base_type,
                    $commissionRule->rate_basis_points,
                    $commissionRule->fixed_minor,
                    $appointment->currency,
                );

                $commissionByStaff[$appointment->staff_profile_id] =
                    ($commissionByStaff[$appointment->staff_profile_id] ?? 0) + $commissionMinor;
            }

            $staffIds = collect(array_keys($approvedHoursByStaff))
                ->merge(array_keys($commissionByStaff))
                ->unique()
                ->values();

            $staffRows = [];
            $grossWagesMinor = 0;
            $grossCommissionMinor = 0;
            $approvedMinutesTotal = 0;

            foreach ($staffIds as $staffId) {
                $approvedMinutes = (int) ($approvedHoursByStaff[$staffId] ?? 0);
                $commissionMinor = (int) ($commissionByStaff[$staffId] ?? 0);

                /** @var WageRule|null $wageRule */
                $wageRule = WageRule::query()
                    ->where('merchant_id', $store->merchant_id)
                    ->where('store_id', $store->id)
                    ->where('staff_profile_id', $staffId)
                    ->where('wage_type', 'hourly')
                    ->where('is_active', true)
                    ->where('effective_from', '<=', $periodEnd->toDateString())
                    ->where(function ($query) use ($periodStart): void {
                        $query->whereNull('effective_to')
                            ->orWhere('effective_to', '>=', $periodStart->toDateString());
                    })
                    ->orderByDesc('effective_from')
                    ->first();

                $hourlyRateMinor = (int) ($wageRule?->hourly_rate_minor ?? 0);
                $wagesMinor = $this->calculateGross($approvedMinutes, $hourlyRateMinor);

                $approvedMinutesTotal += $approvedMinutes;
                $grossWagesMinor += $wagesMinor;
                $grossCommissionMinor += $commissionMinor;

                $staffRows[] = [
                    'staff_profile_id' => $staffId,
                    'approved_minutes' => $approvedMinutes,
                    'hourly_rate_minor' => $hourlyRateMinor,
                    'wages_minor' => $wagesMinor,
                    'commission_minor' => $commissionMinor,
                    'gross_minor' => $wagesMinor + $commissionMinor,
                ];
            }

            return PayrollSnapshot::query()->updateOrCreate(
                [
                    'merchant_id' => $store->merchant_id,
                    'store_id' => $store->id,
                    'period_type' => 'weekly',
                    'period_start' => $periodStart->toDateString(),
                    'period_end' => $periodEnd->toDateString(),
                ],
                [
                    'staff_count' => count($staffRows),
                    'approved_minutes' => $approvedMinutesTotal,
                    'regular_minutes' => $approvedMinutesTotal,
                    'overtime_minutes' => 0,
                    'gross_wages_minor' => $grossWagesMinor,
                    'gross_commission_minor' => $grossCommissionMinor,
                    'gross_pay_minor' => $grossWagesMinor + $grossCommissionMinor,
                    'payload' => [
                        'cycle' => 'weekly',
                        'staff' => $staffRows,
                        'generated_ref' => Str::uuid()->toString(),
                    ],
                    'generated_by_user_id' => $generatedByUserId,
                    'generated_at' => now('UTC'),
                ],
            );
        });
    }
}
