<?php

namespace App\Modules\Retail\Application\Actions;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Domain\Models\BarcodeRecord;

class UpsertBarcodeRecord
{
    /**
     * @param  array<string, mixed>|null  $metadata
     */
    public function handle(
        Store $store,
        string $sku,
        string $barcode,
        ?string $catalogItemId = null,
        bool $isPrimary = false,
        ?array $metadata = null,
    ): BarcodeRecord {
        if ($isPrimary) {
            BarcodeRecord::query()
                ->where('merchant_id', $store->merchant_id)
                ->where('store_id', $store->id)
                ->where('sku', $sku)
                ->update(['is_primary' => false]);
        }

        return BarcodeRecord::query()->updateOrCreate(
            [
                'merchant_id' => $store->merchant_id,
                'store_id' => $store->id,
                'barcode' => $barcode,
            ],
            [
                'catalog_item_id' => $catalogItemId,
                'sku' => $sku,
                'is_primary' => $isPrimary,
                'metadata' => $metadata,
            ],
        );
    }
}
