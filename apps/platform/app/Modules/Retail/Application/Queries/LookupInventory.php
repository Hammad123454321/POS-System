<?php

namespace App\Modules\Retail\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Retail\Domain\Models\BarcodeRecord;
use App\Modules\Retail\Domain\Models\InventoryBalance;

class LookupInventory
{
    /**
     * @return array<string, mixed>|null
     */
    public function handle(Device $device, ?string $sku = null, ?string $barcode = null): ?array
    {
        if (($sku === null || $sku === '') && ($barcode === null || $barcode === '')) {
            return null;
        }

        $resolvedSku = $sku;

        if (($resolvedSku === null || $resolvedSku === '') && $barcode !== null && $barcode !== '') {
            $barcodeRecord = BarcodeRecord::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('barcode', $barcode)
                ->first();

            $resolvedSku = $barcodeRecord?->sku;
        }

        if ($resolvedSku === null || $resolvedSku === '') {
            return null;
        }

        $balance = InventoryBalance::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('sku', $resolvedSku)
            ->first();

        if ($balance === null) {
            return [
                'sku' => $resolvedSku,
                'on_hand_quantity' => 0,
                'reserved_quantity' => 0,
                'available_quantity' => 0,
                'inventory_ledger_seq' => 0,
            ];
        }

        return [
            'id' => $balance->id,
            'sku' => $balance->sku,
            'on_hand_quantity' => (int) $balance->on_hand_quantity,
            'reserved_quantity' => (int) $balance->reserved_quantity,
            'available_quantity' => (int) $balance->available_quantity,
            'inventory_ledger_seq' => (int) $balance->inventory_ledger_seq,
            'last_count_session_id' => $balance->last_count_session_id,
            'last_count_closed_at' => $balance->last_count_closed_at?->toIso8601String(),
        ];
    }
}
