<?php

namespace App\Modules\StoredValue\Application\Actions;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Contracts\StoredValueLedger;
use App\Modules\StoredValue\Domain\Events\GiftCardIssued;
use Illuminate\Support\Facades\Event;

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

        // Meter the issuance (idempotent on the gift-card id).
        Event::dispatch(new GiftCardIssued(
            giftCardId: (string) $giftCard['id'],
            merchantId: $device->merchant_id,
            storeId: $device->store_id,
            amountMinor: $amountMinor,
        ));

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
