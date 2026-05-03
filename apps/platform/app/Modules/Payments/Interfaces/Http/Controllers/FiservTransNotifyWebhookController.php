<?php

namespace App\Modules\Payments\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Payments\Application\Actions\ProcessFiservTransNotifyWebhook;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Throwable;

class FiservTransNotifyWebhookController extends Controller
{
    public function __invoke(Request $request, ProcessFiservTransNotifyWebhook $action): JsonResponse
    {
        $payload = collect($request->all())
            ->mapWithKeys(fn ($value, $key): array => [
                (string) $key => $value === null
                    ? ''
                    : (is_scalar($value) ? (string) $value : json_encode($value)),
            ])
            ->all();

        try {
            $result = $action->handle($payload);

            if (! $result['signature_valid']) {
                return response()->json([
                    'message' => 'Webhook signature verification failed.',
                    'error_code' => 'WEBHOOK_SIGNATURE_INVALID',
                ], 422);
            }

            return response()->json([
                'acknowledged' => true,
                'matched_payment_id' => $result['matched_payment_id'],
                'provider_transaction_id' => $result['provider_transaction_id'],
            ]);
        } catch (Throwable $throwable) {
            report($throwable);

            return response()->json([
                'message' => 'Webhook processing failed.',
                'error_code' => 'WEBHOOK_PROCESSING_FAILED',
            ], 500);
        }
    }
}
