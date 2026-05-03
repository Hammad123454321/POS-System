<?php

namespace App\Modules\Retail\Application\Actions;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Retail\Domain\Models\InventoryAdjustment;
use App\Modules\Retail\Domain\Models\InventoryBalance;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;

class ApplyInventoryLedgerAdjustment
{
    public function __construct(
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    /**
     * @param  array<string, mixed>|null  $metadata
     */
    public function handle(
        Device $device,
        string $sku,
        int $quantityDelta,
        string $adjustmentType,
        ?string $documentNumber = null,
        ?string $reason = null,
        ?string $countSessionId = null,
        ?string $countClosedAt = null,
        ?array $metadata = null,
        ?string $referenceType = null,
        ?string $referenceId = null,
    ): InventoryAdjustment {
        /** @var InventoryAdjustment $adjustment */
        $adjustment = DB::transaction(function () use (
            $adjustmentType,
            $countClosedAt,
            $countSessionId,
            $device,
            $documentNumber,
            $metadata,
            $quantityDelta,
            $reason,
            $referenceId,
            $referenceType,
            $sku
        ): InventoryAdjustment {
            /** @var InventoryBalance $balance */
            $balance = InventoryBalance::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('sku', $sku)
                ->lockForUpdate()
                ->first()
                ?? InventoryBalance::query()->create([
                    'merchant_id' => $device->merchant_id,
                    'store_id' => $device->store_id,
                    'sku' => $sku,
                    'on_hand_quantity' => 0,
                    'reserved_quantity' => 0,
                    'available_quantity' => 0,
                    'inventory_ledger_seq' => 0,
                ]);

            $before = (int) $balance->on_hand_quantity;
            $after = $before + $quantityDelta;
            $nextSeq = (int) $balance->inventory_ledger_seq + 1;

            if ($countSessionId !== null && $countSessionId !== '' && $balance->last_count_session_id !== null && $balance->last_count_session_id !== $countSessionId) {
                $this->openExceptionCase->handle(
                    merchantId: $device->merchant_id,
                    storeId: $device->store_id,
                    module: 'retail',
                    code: 'inventory_conflicting_counts',
                    severity: 'high',
                    title: 'Conflicting inventory count sessions were detected.',
                    details: [
                        'sku' => $sku,
                        'last_count_session_id' => $balance->last_count_session_id,
                        'incoming_count_session_id' => $countSessionId,
                    ],
                    relatedType: 'inventory_balance',
                    relatedId: $balance->id,
                    openedByDeviceId: $device->id,
                );
            }

            $balance->forceFill([
                'on_hand_quantity' => $after,
                'available_quantity' => $after - (int) $balance->reserved_quantity,
                'inventory_ledger_seq' => $nextSeq,
                'last_count_session_id' => $countSessionId ?: $balance->last_count_session_id,
                'last_count_closed_at' => $countClosedAt ?: $balance->last_count_closed_at,
            ])->save();

            if ($after < 0) {
                $this->openExceptionCase->handle(
                    merchantId: $device->merchant_id,
                    storeId: $device->store_id,
                    module: 'retail',
                    code: 'inventory_negative_stock',
                    severity: 'high',
                    title: 'Inventory on-hand quantity moved below zero.',
                    details: [
                        'sku' => $sku,
                        'quantity_before' => $before,
                        'quantity_after' => $after,
                        'adjustment_type' => $adjustmentType,
                    ],
                    relatedType: 'inventory_balance',
                    relatedId: $balance->id,
                    openedByDeviceId: $device->id,
                );
            }

            if ($countSessionId !== null && $countSessionId !== '') {
                $absoluteDelta = abs($quantityDelta);
                $varianceThreshold = max(5, (int) floor(max(1, abs($before)) * 0.05));

                if ($absoluteDelta > $varianceThreshold) {
                    $this->openExceptionCase->handle(
                        merchantId: $device->merchant_id,
                        storeId: $device->store_id,
                        module: 'retail',
                        code: 'inventory_high_variance',
                        severity: 'medium',
                        title: 'Inventory count session variance exceeded threshold.',
                        details: [
                            'sku' => $sku,
                            'quantity_before' => $before,
                            'quantity_after' => $after,
                            'quantity_delta' => $quantityDelta,
                            'variance_threshold' => $varianceThreshold,
                            'count_session_id' => $countSessionId,
                        ],
                        relatedType: 'inventory_balance',
                        relatedId: $balance->id,
                        openedByDeviceId: $device->id,
                    );
                }
            }

            return InventoryAdjustment::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'inventory_balance_id' => $balance->id,
                'sku' => $sku,
                'adjustment_type' => $adjustmentType,
                'quantity_delta' => $quantityDelta,
                'quantity_before' => $before,
                'quantity_after' => $after,
                'count_session_id' => $countSessionId,
                'count_closed_at' => $countClosedAt,
                'document_number' => $documentNumber,
                'reason' => $reason,
                'reference_type' => $referenceType,
                'reference_id' => $referenceId,
                'metadata' => $metadata,
                'created_by_device_id' => $device->id,
                'created_by_user_id' => null,
                'created_at' => CarbonImmutable::now('UTC'),
                'updated_at' => CarbonImmutable::now('UTC'),
            ]);
        });

        return $adjustment;
    }
}
