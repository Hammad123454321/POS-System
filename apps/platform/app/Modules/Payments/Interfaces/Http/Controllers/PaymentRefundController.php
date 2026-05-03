<?php

namespace App\Modules\Payments\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Application\Actions\RefundPayment;
use App\Modules\Payments\Interfaces\Http\Requests\RefundPaymentRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;

class PaymentRefundController extends Controller
{
    public function store(RefundPaymentRequest $request, Payment $payment, RefundPayment $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();
            $refund = $action->handle(
                $device,
                $payment,
                $request->filled('amount_minor') ? (int) $request->integer('amount_minor') : null,
                $request->filled('reason') ? $request->string('reason')->toString() : null,
            );

            return response()->json([
                'data' => [
                    'refund_id' => $refund->id,
                    'payment_id' => $refund->payment_id,
                    'status' => $refund->status,
                    'amount_minor' => $refund->amount_minor,
                    'reason' => $refund->reason,
                    'refunded_at' => $refund->refunded_at?->toIso8601String(),
                ],
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
