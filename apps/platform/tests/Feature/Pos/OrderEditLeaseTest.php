<?php

use App\Modules\PlatformCore\Domain\Models\Device;
use Laravel\Sanctum\Sanctum;

function createOpenOrder($device, $registerSession, $item, string $idem): string
{
    $response = test()->withHeaders(posHeaders() + ['Idempotency-Key' => $idem])
        ->postJson('/api/pos/v1/orders', [
            'register_session_id' => $registerSession->id,
            'lines' => [['catalog_item_id' => $item->id, 'quantity' => 1]],
        ]);
    $response->assertCreated();

    return $response->json('data.id');
}

it('allows tendering when no lease is held (single-device flow)', function () {
    [$device, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

    $orderId = createOpenOrder($device, $session, $item, 'oc-1');

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'co-1'])
        ->postJson("/api/pos/v1/orders/{$orderId}/cash-checkout", ['tendered_minor' => 5000])
        ->assertCreated();
});

it('blocks a second device from tendering while another holds the lease', function () {
    [$deviceA, $session, $item] = buildPosOrderContext();

    $deviceB = Device::query()->create([
        'merchant_id' => $deviceA->merchant_id,
        'store_id' => $deviceA->store_id,
        'device_profile_id' => $deviceA->device_profile_id,
        'name' => 'Device B',
        'platform' => 'android',
        'status' => 'active',
        'drawer_code' => 'drawer-2',
    ]);

    Sanctum::actingAs($deviceA, ['pos:access']);
    $orderId = createOpenOrder($deviceA, $session, $item, 'oc-2');

    // Device A claims the edit lease.
    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'lease-1'])
        ->postJson("/api/pos/v1/orders/{$orderId}/edit-lease")
        ->assertOk()
        ->assertJsonPath('data.holder_device_id', $deviceA->id);

    // Device B tries to cash-checkout → 409 LEASE_CONFLICT.
    Sanctum::actingAs($deviceB, ['pos:access']);
    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'co-b'])
        ->postJson("/api/pos/v1/orders/{$orderId}/cash-checkout", ['tendered_minor' => 5000])
        ->assertStatus(409)
        ->assertJsonPath('error_code', 'LEASE_CONFLICT');
});

it('lets the lease holder tender and releases on close', function () {
    [$deviceA, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($deviceA, ['pos:access']);

    $orderId = createOpenOrder($deviceA, $session, $item, 'oc-3');

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'lease-3'])
        ->postJson("/api/pos/v1/orders/{$orderId}/edit-lease")
        ->assertOk();

    // The holder can heartbeat.
    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'hb-3'])
        ->postJson("/api/pos/v1/orders/{$orderId}/edit-lease/heartbeat")
        ->assertOk();

    // The holder can release.
    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'rel-3'])
        ->postJson("/api/pos/v1/orders/{$orderId}/edit-lease/release")
        ->assertOk();

    // After release, anyone may tender again.
    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'co-3'])
        ->postJson("/api/pos/v1/orders/{$orderId}/cash-checkout", ['tendered_minor' => 5000])
        ->assertCreated();
});
