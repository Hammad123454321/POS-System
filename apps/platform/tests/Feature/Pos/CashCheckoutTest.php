<?php

use App\Modules\OrderRegister\Domain\Models\CashMovement;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\OrderRegister\Domain\Models\Receipt;
use Laravel\Sanctum\Sanctum;

it('creates a cash payment receipt and updates the register session total', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    Sanctum::actingAs($device, ['pos:access']);

    $baseHeaders = posHeaders();

    $orderResponse = $this->withHeaders($baseHeaders + [
        'Idempotency-Key' => 'order-create-1',
    ])->postJson('/api/pos/v1/orders', [
        'register_session_id' => $registerSession->id,
        'lines' => [
            [
                'catalog_item_id' => $item->id,
                'quantity' => 1,
            ],
        ],
    ]);

    $orderResponse->assertCreated();

    $orderId = $orderResponse->json('data.id');

    $checkoutResponse = $this->withHeaders($baseHeaders + [
        'Idempotency-Key' => 'order-checkout-1',
    ])->postJson("/api/pos/v1/orders/{$orderId}/cash-checkout", [
        'tendered_minor' => 2000,
    ]);

    $checkoutResponse->assertCreated();

    $order = Order::query()->findOrFail($orderId);

    expect($order->status)->toBe('paid');
    expect(Payment::query()->count())->toBe(1);
    expect(Receipt::query()->count())->toBe(1);
    expect(CashMovement::query()->where('type', 'sale_cash')->count())->toBe(1);
});
