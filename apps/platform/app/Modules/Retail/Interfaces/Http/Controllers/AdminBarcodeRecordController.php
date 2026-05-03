<?php

namespace App\Modules\Retail\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Application\Actions\UpsertBarcodeRecord;
use App\Modules\Retail\Interfaces\Http\Requests\UpsertBarcodeRecordRequest;
use Illuminate\Http\JsonResponse;

class AdminBarcodeRecordController extends Controller
{
    public function store(
        UpsertBarcodeRecordRequest $request,
        Store $store,
        UpsertBarcodeRecord $action,
    ): JsonResponse {
        $record = $action->handle(
            store: $store,
            sku: $request->string('sku')->toString(),
            barcode: $request->string('barcode')->toString(),
            catalogItemId: $request->filled('catalog_item_id') ? $request->string('catalog_item_id')->toString() : null,
            isPrimary: $request->boolean('is_primary'),
            metadata: $request->input('metadata'),
        );

        return response()->json(['data' => $record], 201);
    }
}
