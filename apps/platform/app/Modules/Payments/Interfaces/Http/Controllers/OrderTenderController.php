<?php

namespace App\Modules\Payments\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\Payments\Application\Actions\TenderOrder;
use App\Modules\Payments\Application\Exceptions\CardInDoubtException;
use App\Modules\Payments\Interfaces\Http\Requests\TenderOrderRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;

class OrderTenderController extends Controller
{
    public function store(
        TenderOrderRequest $request,
        Order $order,
        TenderOrder $action,
        OpenExceptionCase $openExceptionCase,
    ): JsonResponse {
        /** @var Device $device */
        $device = $request->user();

        try {
            $receipt = $action->handle(
                $device,
                $order,
                $request->collect('tenders')
                    ->map(fn (array $tender): array => [
                        'method' => (string) $tender['method'],
                        'amount_minor' => (int) $tender['amount_minor'],
                        'tip_minor' => (int) ($tender['tip_minor'] ?? 0),
                        'tendered_minor' => isset($tender['tendered_minor']) ? (int) $tender['tendered_minor'] : null,
                        'gift_card_code' => $tender['gift_card_code'] ?? null,
                        'provider_key' => $tender['provider_key'] ?? null,
                        'provider_transaction_id' => $tender['provider_transaction_id'] ?? null,
                        'terminal_reference' => $tender['terminal_reference'] ?? null,
                        'auth_code' => $tender['auth_code'] ?? null,
                        'masked_pan' => $tender['masked_pan'] ?? null,
                        'terminal_id' => $tender['terminal_id'] ?? null,
                        'entry_mode' => $tender['entry_mode'] ?? null,
                        'application_label' => $tender['application_label'] ?? null,
                        'aid' => $tender['aid'] ?? null,
                        'tvr' => $tender['tvr'] ?? null,
                        'tsi' => $tender['tsi'] ?? null,
                        'terminal_status_code' => $tender['terminal_status_code'] ?? null,
                        'terminal_result_code' => $tender['terminal_result_code'] ?? null,
                        'terminal_timestamp' => $tender['terminal_timestamp'] ?? null,
                    ])->values()->all(),
            );

            return response()->json([
                'data' => [
                    'receipt_id' => $receipt->id,
                    'receipt_number' => $receipt->receipt_number,
                    'payload' => $receipt->payload,
                ],
            ], 201);
        } catch (CardInDoubtException $exception) {
            $existingCase = ExceptionCase::query()
                ->where('merchant_id', $order->merchant_id)
                ->where('store_id', $order->store_id)
                ->where('module', 'payments')
                ->where('code', 'card_in_doubt')
                ->where('status', 'open')
                ->where('related_type', 'order')
                ->where('related_id', $order->id)
                ->first();

            if ($existingCase === null) {
                $openExceptionCase->handle(
                    merchantId: $order->merchant_id,
                    storeId: $order->store_id,
                    module: 'payments',
                    code: 'card_in_doubt',
                    severity: 'high',
                    title: 'Card terminal in-doubt transaction requires manager recovery.',
                    details: [
                        'order_id' => $order->id,
                        ...$exception->details,
                    ],
                    relatedType: 'order',
                    relatedId: $order->id,
                    openedByDeviceId: $device->id,
                );
            }

            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => 'CARD_IN_DOUBT',
            ], 409);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
