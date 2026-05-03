<?php

namespace App\Modules\Retail\Application\Actions;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Domain\Models\InventoryAdjustment;
use App\Modules\Retail\Domain\Models\InventoryBalance;
use App\Modules\Retail\Domain\Models\InventoryTransfer;
use Illuminate\Support\Facades\DB;

class TransferStock
{
    public function __construct(
        private readonly ApplyInventoryLedgerAdjustment $applyInventoryLedgerAdjustment,
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    /**
     * @param  array<int, array{sku: string, quantity: int}>  $lines
     */
    public function handle(
        Device $device,
        Store $destinationStore,
        string $documentNumber,
        array $lines,
        ?string $reason = null,
    ): InventoryTransfer {
        $existingTransfer = InventoryTransfer::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('document_number', $documentNumber)
            ->first();

        if ($existingTransfer !== null) {
            $this->openExceptionCase->handle(
                merchantId: $device->merchant_id,
                storeId: $device->store_id,
                module: 'retail',
                code: 'inventory_duplicate_transfer_doc',
                severity: 'medium',
                title: 'Duplicate transfer document detected.',
                details: ['document_number' => $documentNumber],
                relatedType: 'inventory_transfer',
                relatedId: $existingTransfer->id,
                openedByDeviceId: $device->id,
            );

            return $existingTransfer;
        }

        /** @var InventoryTransfer $transfer */
        $transfer = DB::transaction(function () use ($destinationStore, $device, $documentNumber, $lines, $reason): InventoryTransfer {
            $transfer = InventoryTransfer::query()->create([
                'merchant_id' => $device->merchant_id,
                'source_store_id' => $device->store_id,
                'destination_store_id' => $destinationStore->id,
                'document_number' => $documentNumber,
                'status' => 'posted',
                'lines' => $lines,
                'reason' => $reason,
                'created_by_device_id' => $device->id,
                'transferred_at' => now('UTC'),
            ]);

            foreach ($lines as $line) {
                $this->applyInventoryLedgerAdjustment->handle(
                    device: $device,
                    sku: $line['sku'],
                    quantityDelta: -1 * (int) $line['quantity'],
                    adjustmentType: 'transfer_out',
                    documentNumber: $documentNumber,
                    reason: $reason,
                    referenceType: 'inventory_transfer',
                    referenceId: $transfer->id,
                );

                /** @var InventoryBalance $destinationBalance */
                $destinationBalance = InventoryBalance::query()
                    ->where('merchant_id', $device->merchant_id)
                    ->where('store_id', $destinationStore->id)
                    ->where('sku', $line['sku'])
                    ->lockForUpdate()
                    ->first()
                    ?? InventoryBalance::query()->create([
                        'merchant_id' => $device->merchant_id,
                        'store_id' => $destinationStore->id,
                        'sku' => $line['sku'],
                        'on_hand_quantity' => 0,
                        'reserved_quantity' => 0,
                        'available_quantity' => 0,
                        'inventory_ledger_seq' => 0,
                    ]);

                $before = (int) $destinationBalance->on_hand_quantity;
                $delta = (int) $line['quantity'];
                $after = $before + $delta;
                $destinationBalance->forceFill([
                    'on_hand_quantity' => $after,
                    'available_quantity' => $after - (int) $destinationBalance->reserved_quantity,
                    'inventory_ledger_seq' => (int) $destinationBalance->inventory_ledger_seq + 1,
                ])->save();

                InventoryAdjustment::query()->create([
                    'merchant_id' => $device->merchant_id,
                    'store_id' => $destinationStore->id,
                    'inventory_balance_id' => $destinationBalance->id,
                    'sku' => $line['sku'],
                    'adjustment_type' => 'transfer_in',
                    'quantity_delta' => $delta,
                    'quantity_before' => $before,
                    'quantity_after' => $after,
                    'document_number' => $documentNumber,
                    'reason' => $reason,
                    'reference_type' => 'inventory_transfer',
                    'reference_id' => $transfer->id,
                    'created_by_device_id' => $device->id,
                ]);
            }

            return $transfer;
        });

        return $transfer;
    }
}
