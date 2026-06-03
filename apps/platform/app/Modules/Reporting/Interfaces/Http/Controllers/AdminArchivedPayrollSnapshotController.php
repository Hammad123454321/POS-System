<?php

namespace App\Modules\Reporting\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Archive\ArchiveAccessLogger;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Domain\Models\PayrollSnapshot;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminArchivedPayrollSnapshotController extends Controller
{
    public function __invoke(
        Request $request,
        Store $store,
        PayrollSnapshot $payrollSnapshot,
        ArchiveAccessLogger $archiveAccessLogger,
    ): JsonResponse {
        abort_unless(
            $payrollSnapshot->merchant_id === $store->merchant_id
            && $payrollSnapshot->store_id === $store->id,
            404,
        );

        $archiveAccessLogger->log(
            merchantId: $payrollSnapshot->merchant_id,
            storeId: $payrollSnapshot->store_id,
            archiveType: 'payroll_snapshot',
            archiveRecordId: $payrollSnapshot->id,
            userId: (int) $request->user()->getAuthIdentifier(),
            deviceId: null,
            reason: $request->query('reason', 'back-office payroll archive read'),
            metadata: [
                'period_type' => $payrollSnapshot->period_type,
                'period_start' => $payrollSnapshot->period_start?->toDateString(),
                'period_end' => $payrollSnapshot->period_end?->toDateString(),
            ],
        );

        return response()->json([
            'data' => [
                'id' => $payrollSnapshot->id,
                'period_type' => $payrollSnapshot->period_type,
                'period_start' => $payrollSnapshot->period_start?->toDateString(),
                'period_end' => $payrollSnapshot->period_end?->toDateString(),
                'staff_count' => $payrollSnapshot->staff_count,
                'approved_minutes' => $payrollSnapshot->approved_minutes,
                'gross_pay_minor' => $payrollSnapshot->gross_pay_minor,
                'payload' => $payrollSnapshot->payload,
                'generated_at' => $payrollSnapshot->generated_at?->toIso8601String(),
            ],
        ]);
    }
}
