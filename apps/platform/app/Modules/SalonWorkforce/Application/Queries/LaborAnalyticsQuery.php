<?php

namespace App\Modules\SalonWorkforce\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Contracts\PayrollCalculator;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use App\Modules\SalonWorkforce\Domain\Models\AttendanceRecord;
use App\Modules\SalonWorkforce\Domain\Models\Shift;
use App\Modules\SalonWorkforce\Domain\Models\StaffServiceRule;
use App\Modules\SalonWorkforce\Domain\Models\WageRule;
use Carbon\CarbonImmutable;

class LaborAnalyticsQuery
{
    public function __construct(
        private readonly PayrollCalculator $payrollCalculator,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        Store $store,
        CarbonImmutable $periodStart,
        CarbonImmutable $periodEnd,
    ): array {
        $attendanceRecords = AttendanceRecord::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereBetween('business_date', [$periodStart->toDateString(), $periodEnd->toDateString()])
            ->get();

        $approvedMinutes = (int) $attendanceRecords
            ->where('approval_status', 'approved')
            ->sum('worked_minutes');

        $scheduledMinutes = (int) Shift::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereBetween('business_date', [$periodStart->toDateString(), $periodEnd->toDateString()])
            ->get()
            ->sum(function (Shift $shift): int {
                if ($shift->scheduled_start_at === null || $shift->scheduled_end_at === null) {
                    return 0;
                }

                return max(0, $shift->scheduled_start_at->diffInMinutes($shift->scheduled_end_at));
            });

        $appointments = Appointment::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereBetween('business_date', [$periodStart->toDateString(), $periodEnd->toDateString()])
            ->whereIn('status', ['confirmed', 'checked_in', 'completed'])
            ->get();

        $completedAppointments = $appointments->where('status', 'completed');
        $assignments = StaffServiceRule::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->where('is_active', true)
            ->with('commissionRule')
            ->get()
            ->keyBy(fn (StaffServiceRule $rule): string => "{$rule->staff_profile_id}:{$rule->service_item_id}");

        $commissionMinor = 0;

        foreach ($completedAppointments as $appointment) {
            $rule = $assignments->get("{$appointment->staff_profile_id}:{$appointment->service_item_id}")?->commissionRule;

            if ($rule === null || ! $rule->is_active) {
                continue;
            }

            $commissionMinor += $this->payrollCalculator->calculateCommission(
                $appointment->service_price_minor,
                $appointment->discount_minor,
                $rule->base_type,
                $rule->rate_basis_points,
                $rule->fixed_minor,
                $appointment->currency,
            );
        }

        $hourTotalsByStaff = $this->payrollCalculator->snapshotApprovedHours($store, $periodStart, $periodEnd);
        $wagesMinor = 0;

        foreach ($hourTotalsByStaff as $staffProfileId => $approvedMinutesForStaff) {
            /** @var WageRule|null $wageRule */
            $wageRule = WageRule::query()
                ->where('merchant_id', $store->merchant_id)
                ->where('store_id', $store->id)
                ->where('staff_profile_id', $staffProfileId)
                ->where('wage_type', 'hourly')
                ->where('is_active', true)
                ->where('effective_from', '<=', $periodEnd->toDateString())
                ->where(function ($query) use ($periodStart): void {
                    $query->whereNull('effective_to')
                        ->orWhere('effective_to', '>=', $periodStart->toDateString());
                })
                ->orderByDesc('effective_from')
                ->first();

            $wagesMinor += $this->payrollCalculator->calculateGross(
                $approvedMinutesForStaff,
                (int) ($wageRule?->hourly_rate_minor ?? 0),
            );
        }

        $utilizationBasisPoints = $scheduledMinutes <= 0
            ? 0
            : (int) floor(($approvedMinutes * 10000) / $scheduledMinutes);

        return [
            'period_start' => $periodStart->toDateString(),
            'period_end' => $periodEnd->toDateString(),
            'scheduled_minutes' => $scheduledMinutes,
            'approved_minutes' => $approvedMinutes,
            'appointments_booked_count' => $appointments->count(),
            'appointments_completed_count' => $completedAppointments->count(),
            'utilization_basis_points' => $utilizationBasisPoints,
            'estimated_wages_minor' => $wagesMinor,
            'estimated_commission_minor' => $commissionMinor,
            'estimated_labor_cost_minor' => $wagesMinor + $commissionMinor,
        ];
    }
}
