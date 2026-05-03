<?php

namespace App\Modules\Retail\Application\Actions;

use App\Modules\PlatformCore\Domain\Models\Device;

class ProcessItemReturn
{
    public function __construct(
        private readonly ReceiveStock $receiveStock,
    ) {}

    /**
     * @param  array<int, array{sku: string, quantity: int}>  $lines
     */
    public function handle(
        Device $device,
        string $documentNumber,
        array $lines,
        ?string $reason = null,
    ): array {
        $record = $this->receiveStock->handle(
            device: $device,
            documentNumber: $documentNumber,
            lines: $lines,
            supplierName: null,
            reason: $reason ?? 'item_return',
        );

        return [
            'id' => $record->id,
            'status' => 'posted',
            'document_number' => $record->document_number,
            'received_at' => $record->received_at?->toIso8601String(),
        ];
    }
}
