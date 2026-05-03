<?php

namespace App\Modules\Retail\Application\Actions;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Retail\Domain\Models\InventoryAdjustment;

class AdjustStock
{
    public function __construct(
        private readonly ApplyInventoryLedgerAdjustment $applyInventoryLedgerAdjustment,
    ) {}

    public function handle(
        Device $device,
        string $sku,
        int $quantityDelta,
        string $reason,
        ?string $documentNumber = null,
        ?string $countSessionId = null,
        ?string $countClosedAt = null,
    ): InventoryAdjustment {
        return $this->applyInventoryLedgerAdjustment->handle(
            device: $device,
            sku: $sku,
            quantityDelta: $quantityDelta,
            adjustmentType: 'manual_adjustment',
            documentNumber: $documentNumber,
            reason: $reason,
            countSessionId: $countSessionId,
            countClosedAt: $countClosedAt,
        );
    }
}
