<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\OrderRegister\Domain\Models\Receipt;
use App\Modules\OrderRegister\Interfaces\Mail\ReceiptMail;
use App\Modules\PlatformCore\Domain\Models\Device;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Mail;

class ReceiptEmailController extends Controller
{
    public function __invoke(Request $request, Receipt $receipt, AuditLogger $audit): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        abort_unless($receipt->store_id === $device->store_id, 404);

        $validated = $request->validate([
            'email' => ['required', 'email', 'max:160'],
        ]);

        Mail::to($validated['email'])->send(new ReceiptMail(
            (string) $receipt->receipt_number,
            (array) $receipt->payload,
        ));

        $audit->log(
            merchantId: $receipt->merchant_id,
            storeId: $receipt->store_id,
            module: 'order_register',
            action: 'receipt.emailed',
            subjectType: 'receipt',
            subjectId: $receipt->id,
            metadata: ['email' => $validated['email']],
            deviceId: $device->id,
        );

        return response()->json(['data' => ['status' => 'queued']]);
    }
}
