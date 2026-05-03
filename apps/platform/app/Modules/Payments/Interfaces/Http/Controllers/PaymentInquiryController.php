<?php

namespace App\Modules\Payments\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Application\PaymentProviderRegistry;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class PaymentInquiryController extends Controller
{
    public function __invoke(
        Request $request,
        Payment $payment,
        PaymentProviderRegistry $paymentProviderRegistry,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            if ($payment->merchant_id !== $device->merchant_id || $payment->store_id !== $device->store_id) {
                throw new DomainException('The requested payment does not belong to this device context.');
            }

            if ($payment->method !== 'card') {
                throw new DomainException('Only card payments support provider inquiry.');
            }

            if (! is_string($payment->provider_transaction_id) || trim($payment->provider_transaction_id) === '') {
                throw new DomainException('Payment is missing provider transaction reference.');
            }

            $provider = $paymentProviderRegistry->forKey($payment->provider_key);
            $inquiry = $provider->inquire($device, $payment->provider_transaction_id);

            return response()->json([
                'data' => [
                    'payment_id' => $payment->id,
                    'provider_key' => $payment->provider_key,
                    ...$inquiry,
                ],
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
