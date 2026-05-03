<?php

namespace App\Modules\Payments\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Application\Actions\VoidPayment;
use App\Modules\Payments\Interfaces\Http\Requests\VoidPaymentRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;

class PaymentVoidController extends Controller
{
    public function store(VoidPaymentRequest $request, Payment $payment, VoidPayment $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();
            $void = $action->handle(
                $device,
                $payment,
                $request->filled('reason') ? $request->string('reason')->toString() : null,
            );

            return response()->json([
                'data' => [
                    'void_record_id' => $void->id,
                    'payment_id' => $void->payment_id,
                    'status' => $void->status,
                    'reason' => $void->reason,
                    'voided_at' => $void->voided_at?->toIso8601String(),
                ],
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
