<?php

namespace App\Modules\StoredValue\Application\Actions;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Contracts\StoredValueLedger;

class IssueGiftCard
{
    public function __construct(
        private readonly StoredValueLedger $storedValueLedger,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        Device $device,
        int $amountMinor,
        ?string $customerId = null,
        ?string $requestedCode = null,
    ): array {
        $giftCard = $this->storedValueLedger->issue(
            $device->store()->firstOrFail(),
            $customerId,
            $amountMinor,
            $requestedCode,
        );

        return [
            ...$giftCard,
            'label_payload' => [
                'document_type' => 'label',
                'gift_card_code' => $giftCard['code'],
                'balance_minor' => $giftCard['current_balance_minor'],
            ],
        ];
    }
}
