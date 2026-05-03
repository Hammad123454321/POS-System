<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OrderRegister\Application\Actions\CashCheckoutOrder;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Interfaces\Http\Requests\CashCheckoutRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;

class OrderCashCheckoutController extends Controller
{
    public function store(CashCheckoutRequest $request, Order $order, CashCheckoutOrder $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();
            $receipt = $action->handle($device, $order, (int) $request->integer('tendered_minor'));

            return response()->json([
                'data' => [
                    'receipt_id' => $receipt->id,
                    'receipt_number' => $receipt->receipt_number,
                    'payload' => $receipt->payload,
                ],
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
