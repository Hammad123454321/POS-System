<?php

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\Retail\Domain\Models\InventoryBalance;
use Laravel\Sanctum\Sanctum;

function seedOrder($device, $session, string $status = 'open', int $seq = 1): Order
{
    return Order::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'register_session_id' => $session->id,
        'device_id' => $device->id,
        'order_number' => 'SA-'.uniqid(),
        'status' => $status,
        'status_seq' => $seq,
        'business_date' => '2026-05-01',
        'currency' => 'USD',
        'subtotal_minor' => 1000,
        'tax_minor' => 100,
        'discount_minor' => 0,
        'total_minor' => 1100,
        'paid_minor' => 0,
        'opened_at' => now(),
    ]);
}

it('applies a legal transition on a fresh base and increments status_seq', function () {
    [$device, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);
    $order = seedOrder($device, $session, 'open', 1);

    $resp = $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'sa-1'])
        ->postJson('/api/pos/v1/sync/events', [
            'events' => [[
                'local_event_id' => 'sa-1',
                'entity_type' => 'order',
                'entity_id' => $order->id,
                'action' => 'status_change',
                'payload' => ['base_status_seq' => 1, 'status' => 'sent'],
            ]],
        ])->assertSuccessful();

    expect($order->fresh()->status)->toBe('sent');
    expect((int) $order->fresh()->status_seq)->toBe(2);
});

it('supersedes a stale base sequence and opens an exception for money-bearing changes', function () {
    [$device, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);
    $order = seedOrder($device, $session, 'sent', 3);

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'sa-2'])
        ->postJson('/api/pos/v1/sync/events', [
            'events' => [[
                'local_event_id' => 'sa-2',
                'entity_type' => 'order',
                'entity_id' => $order->id,
                'action' => 'status_change',
                'payload' => ['base_status_seq' => 1, 'status' => 'paid'],
            ]],
        ])->assertSuccessful();

    // Order unchanged (server wins).
    expect($order->fresh()->status)->toBe('sent');
    expect(ExceptionCase::query()->where('code', 'order_conflict')->exists())->toBeTrue();
});

it('rejects an illegal transition on a fresh base', function () {
    [$device, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);
    $order = seedOrder($device, $session, 'paid', 5);

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'sa-3'])
        ->postJson('/api/pos/v1/sync/events', [
            'events' => [[
                'local_event_id' => 'sa-3',
                'entity_type' => 'order',
                'entity_id' => $order->id,
                'action' => 'status_change',
                'payload' => ['base_status_seq' => 5, 'status' => 'open'],
            ]],
        ])->assertSuccessful();

    expect($order->fresh()->status)->toBe('paid');
    expect(ExceptionCase::query()->where('code', 'order_illegal_transition')->exists())->toBeTrue();
});

it('rejects an absolute inventory set on a stale ledger sequence', function () {
    [$device, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

    InventoryBalance::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'sku' => 'WIDGET-1',
        'on_hand_quantity' => 10,
        'reserved_quantity' => 0,
        'available_quantity' => 10,
        'inventory_ledger_seq' => 7,
    ]);

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'sa-inv-1'])
        ->postJson('/api/pos/v1/sync/events', [
            'events' => [[
                'local_event_id' => 'sa-inv-1',
                'entity_type' => 'inventory',
                'entity_id' => null,
                'action' => 'set_on_hand',
                'payload' => ['sku' => 'WIDGET-1', 'base_ledger_seq' => 3, 'on_hand' => 99],
            ]],
        ])->assertSuccessful();

    expect(ExceptionCase::query()->where('code', 'inventory_conflict')->exists())->toBeTrue();
});

it('accepts unrelated entity types unchanged', function () {
    [$device, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'sa-4'])
        ->postJson('/api/pos/v1/sync/events', [
            'events' => [[
                'local_event_id' => 'sa-4',
                'entity_type' => 'note',
                'entity_id' => null,
                'action' => 'create',
                'payload' => ['text' => 'hi'],
            ]],
        ])->assertSuccessful();

    expect(true)->toBeTrue();
});
