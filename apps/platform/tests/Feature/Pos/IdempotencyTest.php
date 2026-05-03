<?php

use App\Modules\OrderRegister\Domain\Models\Order;
use Laravel\Sanctum\Sanctum;

it('replays the original response for an idempotent order create request', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    Sanctum::actingAs($device, ['pos:access']);

    $headers = posHeaders([
        'Idempotency-Key' => 'create-order-1',
    ]);

    $payload = [
        'register_session_id' => $registerSession->id,
        'lines' => [
            [
                'catalog_item_id' => $item->id,
                'quantity' => 2,
            ],
        ],
    ];

    $first = $this->withHeaders($headers)->postJson('/api/pos/v1/orders', $payload);
    $second = $this->withHeaders($headers)->postJson('/api/pos/v1/orders', $payload);

    $first->assertCreated();
    $second->assertCreated();

    expect(Order::query()->count())->toBe(1);
    expect($second->json('data.id'))->toBe($first->json('data.id'));
});

it('rejects idempotency key reuse when the payload changes', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    Sanctum::actingAs($device, ['pos:access']);

    $headers = posHeaders([
        'Idempotency-Key' => 'create-order-2',
    ]);

    $firstPayload = [
        'register_session_id' => $registerSession->id,
        'lines' => [
            [
                'catalog_item_id' => $item->id,
                'quantity' => 1,
            ],
        ],
    ];

    $secondPayload = [
        'register_session_id' => $registerSession->id,
        'lines' => [
            [
                'catalog_item_id' => $item->id,
                'quantity' => 2,
            ],
        ],
    ];

    $this->withHeaders($headers)
        ->postJson('/api/pos/v1/orders', $firstPayload)
        ->assertCreated();

    $this->withHeaders($headers)
        ->postJson('/api/pos/v1/orders', $secondPayload)
        ->assertStatus(422)
        ->assertJsonPath('message', 'Idempotency key reuse with a different payload is not allowed.');
});
