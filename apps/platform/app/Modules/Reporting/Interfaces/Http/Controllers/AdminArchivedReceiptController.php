<?php

namespace App\Modules\Reporting\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OrderRegister\Domain\Models\Receipt;
use App\Modules\PlatformCore\Application\Archive\ArchiveAccessLogger;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminArchivedReceiptController extends Controller
{
    public function __invoke(
        Request $request,
        Store $store,
        Receipt $receipt,
        ArchiveAccessLogger $archiveAccessLogger,
    ): JsonResponse {
        abort_unless($receipt->merchant_id === $store->merchant_id && $receipt->store_id === $store->id, 404);

        $archiveAccessLogger->log(
            merchantId: $receipt->merchant_id,
            storeId: $receipt->store_id,
            archiveType: 'receipt',
            archiveRecordId: $receipt->id,
            userId: (int) $request->user()->getAuthIdentifier(),
            deviceId: null,
            reason: $request->query('reason', 'back-office receipt archive read'),
            metadata: [
                'receipt_number' => $receipt->receipt_number,
                'order_id' => $receipt->order_id,
            ],
        );

        return response()->json([
            'data' => [
                'id' => $receipt->id,
                'receipt_number' => $receipt->receipt_number,
                'order_id' => $receipt->order_id,
                'payment_id' => $receipt->payment_id,
                'payload' => $receipt->payload,
                'printed_at' => $receipt->printed_at?->toIso8601String(),
                'created_at' => $receipt->created_at?->toIso8601String(),
            ],
        ]);
    }
}
