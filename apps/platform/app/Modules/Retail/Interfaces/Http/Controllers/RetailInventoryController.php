<?php

namespace App\Modules\Retail\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Application\Actions\AdjustStock;
use App\Modules\Retail\Application\Actions\ProcessItemReturn;
use App\Modules\Retail\Application\Actions\ReceiveStock;
use App\Modules\Retail\Application\Actions\TransferStock;
use App\Modules\Retail\Application\Queries\LookupInventory;
use App\Modules\Retail\Interfaces\Http\Requests\AdjustStockRequest;
use App\Modules\Retail\Interfaces\Http\Requests\InventoryLookupRequest;
use App\Modules\Retail\Interfaces\Http\Requests\ProcessReturnRequest;
use App\Modules\Retail\Interfaces\Http\Requests\ReceiveStockRequest;
use App\Modules\Retail\Interfaces\Http\Requests\TransferStockRequest;
use DomainException;
use Illuminate\Http\JsonResponse;

class RetailInventoryController extends Controller
{
    public function lookup(InventoryLookupRequest $request, LookupInventory $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        $result = $query->handle(
            device: $device,
            sku: $request->filled('sku') ? $request->string('sku')->toString() : null,
            barcode: $request->filled('barcode') ? $request->string('barcode')->toString() : null,
        );

        return response()->json(['data' => $result]);
    }

    public function receive(ReceiveStockRequest $request, ReceiveStock $action): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        $record = $action->handle(
            device: $device,
            documentNumber: $request->string('document_number')->toString(),
            lines: $request->input('lines', []),
            supplierName: $request->filled('supplier_name') ? $request->string('supplier_name')->toString() : null,
            reason: $request->filled('reason') ? $request->string('reason')->toString() : null,
        );

        return response()->json(['data' => $record], 201);
    }

    public function transfer(TransferStockRequest $request, TransferStock $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();
            /** @var Store $destinationStore */
            $destinationStore = Store::query()
                ->where('merchant_id', $device->merchant_id)
                ->whereKey($request->string('destination_store_id')->toString())
                ->firstOrFail();

            $transfer = $action->handle(
                device: $device,
                destinationStore: $destinationStore,
                documentNumber: $request->string('document_number')->toString(),
                lines: $request->input('lines', []),
                reason: $request->filled('reason') ? $request->string('reason')->toString() : null,
            );

            return response()->json(['data' => $transfer], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function adjust(AdjustStockRequest $request, AdjustStock $action): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        $adjustment = $action->handle(
            device: $device,
            sku: $request->string('sku')->toString(),
            quantityDelta: (int) $request->integer('quantity_delta'),
            reason: $request->string('reason')->toString(),
            documentNumber: $request->filled('document_number') ? $request->string('document_number')->toString() : null,
            countSessionId: $request->filled('count_session_id') ? $request->string('count_session_id')->toString() : null,
            countClosedAt: $request->filled('count_closed_at') ? $request->string('count_closed_at')->toString() : null,
        );

        return response()->json(['data' => $adjustment], 201);
    }

    public function processReturn(ProcessReturnRequest $request, ProcessItemReturn $action): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        $result = $action->handle(
            device: $device,
            documentNumber: $request->string('document_number')->toString(),
            lines: $request->input('lines', []),
            reason: $request->filled('reason') ? $request->string('reason')->toString() : null,
        );

        return response()->json(['data' => $result], 201);
    }
}
