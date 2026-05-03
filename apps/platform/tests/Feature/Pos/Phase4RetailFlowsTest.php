<?php

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Domain\Models\InventoryAdjustment;
use App\Modules\Retail\Domain\Models\InventoryBalance;
use App\Modules\Retail\Domain\Models\InventoryTransfer;
use App\Modules\Retail\Domain\Models\ReceivingRecord;
use Laravel\Sanctum\Sanctum;

it('handles receiving lookup adjustment transfer and returns through retail pos endpoints', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    /** @var Store $secondStore */
    $secondStore = Store::query()->create([
        'merchant_id' => $device->merchant_id,
        'name' => 'Store B',
        'code' => 'STR-B',
        'mode' => 'retail',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-retail-receive',
    ]))->postJson('/api/pos/v1/retail/inventory/receive', [
        'document_number' => 'REC-1001',
        'supplier_name' => 'Local Supplier',
        'lines' => [
            ['sku' => $item->sku, 'quantity' => 10],
        ],
    ])->assertCreated()
        ->assertJsonPath('data.document_number', 'REC-1001');

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/retail/inventory/lookup?sku='.$item->sku)
        ->assertOk()
        ->assertJsonPath('data.on_hand_quantity', 10);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-retail-adjust',
    ]))->postJson('/api/pos/v1/retail/inventory/adjust', [
        'sku' => $item->sku,
        'quantity_delta' => -3,
        'reason' => 'damaged',
        'document_number' => 'ADJ-1001',
    ])->assertCreated();

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-retail-transfer',
    ]))->postJson('/api/pos/v1/retail/inventory/transfer', [
        'document_number' => 'TRN-1001',
        'destination_store_id' => $secondStore->id,
        'lines' => [
            ['sku' => $item->sku, 'quantity' => 4],
        ],
    ])->assertCreated();

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-retail-return',
    ]))->postJson('/api/pos/v1/retail/inventory/returns', [
        'document_number' => 'RTN-1001',
        'lines' => [
            ['sku' => $item->sku, 'quantity' => 1],
        ],
    ])->assertCreated();

    expect(ReceivingRecord::query()->where('document_number', 'REC-1001')->exists())->toBeTrue();
    expect(InventoryTransfer::query()->where('document_number', 'TRN-1001')->exists())->toBeTrue();
    expect(InventoryBalance::query()->where('store_id', $device->store_id)->where('sku', $item->sku)->firstOrFail()->on_hand_quantity)->toBe(4);
    expect(InventoryBalance::query()->where('store_id', $secondStore->id)->where('sku', $item->sku)->firstOrFail()->on_hand_quantity)->toBe(4);
    expect(InventoryAdjustment::query()->where('store_id', $device->store_id)->where('sku', $item->sku)->count())->toBeGreaterThan(2);
});

it('creates retail exception cases for duplicate documents and negative stock', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-retail-receive-dup-1',
    ]))->postJson('/api/pos/v1/retail/inventory/receive', [
        'document_number' => 'REC-DUP-1',
        'lines' => [
            ['sku' => $item->sku, 'quantity' => 1],
        ],
    ])->assertCreated();

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-retail-receive-dup-2',
    ]))->postJson('/api/pos/v1/retail/inventory/receive', [
        'document_number' => 'REC-DUP-1',
        'lines' => [
            ['sku' => $item->sku, 'quantity' => 1],
        ],
    ])->assertCreated();

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-4-retail-negative',
    ]))->postJson('/api/pos/v1/retail/inventory/adjust', [
        'sku' => $item->sku,
        'quantity_delta' => -100,
        'reason' => 'forced negative for test',
    ])->assertCreated();

    expect(ExceptionCase::query()
        ->where('merchant_id', $device->merchant_id)
        ->where('store_id', $device->store_id)
        ->where('module', 'retail')
        ->whereIn('code', ['inventory_duplicate_receiving_doc', 'inventory_negative_stock'])
        ->count())->toBeGreaterThanOrEqual(2);
});
