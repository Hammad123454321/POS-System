<?php

namespace App\Modules\Retail\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Retail\Domain\Models\BarcodeRecord;

class RetailConfigSnapshotQuery
{
    /**
     * @return array<string, mixed>
     */
    public function forDevice(Device $device): array
    {
        $barcodes = BarcodeRecord::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->orderByDesc('is_primary')
            ->orderBy('sku')
            ->get(['id', 'sku', 'barcode', 'is_primary']);

        return [
            'barcode_records' => $barcodes->map(fn (BarcodeRecord $record): array => [
                'id' => $record->id,
                'sku' => $record->sku,
                'barcode' => $record->barcode,
                'is_primary' => $record->is_primary,
            ])->values()->all(),
        ];
    }
}
