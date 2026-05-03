<?php

namespace App\Modules\Retail\Application\Actions;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Retail\Domain\Models\ReceivingRecord;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;

class ReceiveStock
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
        string $documentNumber,
        array $lines,
        ?string $supplierName = null,
        ?string $reason = null,
    ): ReceivingRecord {
        $existingRecord = ReceivingRecord::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('document_number', $documentNumber)
            ->first();

        if ($existingRecord !== null) {
            $this->openExceptionCase->handle(
                merchantId: $device->merchant_id,
                storeId: $device->store_id,
                module: 'retail',
                code: 'inventory_duplicate_receiving_doc',
                severity: 'medium',
                title: 'Duplicate receiving document detected.',
                details: ['document_number' => $documentNumber],
                relatedType: 'receiving_record',
                relatedId: $existingRecord->id,
                openedByDeviceId: $device->id,
            );

            return $existingRecord;
        }

        /** @var ReceivingRecord $record */
        $record = DB::transaction(function () use ($device, $documentNumber, $lines, $supplierName, $reason): ReceivingRecord {
            $record = ReceivingRecord::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'document_number' => $documentNumber,
                'status' => 'posted',
                'lines' => $lines,
                'supplier_name' => $supplierName,
                'reason' => $reason,
                'created_by_device_id' => $device->id,
                'received_at' => CarbonImmutable::now('UTC'),
            ]);

            foreach ($lines as $line) {
                $this->applyInventoryLedgerAdjustment->handle(
                    device: $device,
                    sku: $line['sku'],
                    quantityDelta: (int) $line['quantity'],
                    adjustmentType: 'receiving',
                    documentNumber: $documentNumber,
                    reason: $reason,
                    metadata: ['supplier_name' => $supplierName],
                    referenceType: 'receiving_record',
                    referenceId: $record->id,
                );
            }

            return $record;
        });

        return $record;
    }
}
