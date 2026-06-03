<?php

namespace App\Modules\CustomerValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\CustomerValue\Application\Privacy\ExportCustomerData;
use App\Modules\CustomerValue\Application\Privacy\TombstoneCustomerPii;
use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\PlatformCore\Application\Archive\ArchiveAccessLogger;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminCustomerPrivacyController extends Controller
{
    public function export(
        Request $request,
        Store $store,
        Customer $customer,
        ExportCustomerData $exportCustomerData,
        ArchiveAccessLogger $archiveAccessLogger,
    ): JsonResponse {
        abort_unless($customer->merchant_id === $store->merchant_id, 404);

        $result = $exportCustomerData->handle($customer, (int) $request->user()->getAuthIdentifier());
        $archiveAccessLogger->log(
            merchantId: $customer->merchant_id,
            storeId: $store->id,
            archiveType: 'customer_privacy_export',
            archiveRecordId: $customer->id,
            userId: (int) $request->user()->getAuthIdentifier(),
            deviceId: null,
            reason: $request->string('reason', 'customer export request')->toString(),
            metadata: $result,
        );

        return response()->json(['data' => $result], 202);
    }

    public function tombstone(
        Request $request,
        Store $store,
        Customer $customer,
        TombstoneCustomerPii $tombstoneCustomerPii,
    ): JsonResponse {
        abort_unless($customer->merchant_id === $store->merchant_id, 404);
        $request->validate([
            'reason' => ['required', 'string', 'max:500'],
        ]);

        return response()->json([
            'data' => $tombstoneCustomerPii->handle(
                $customer,
                (int) $request->user()->getAuthIdentifier(),
                $request->string('reason')->toString(),
            ),
        ]);
    }
}
