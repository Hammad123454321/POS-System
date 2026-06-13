<?php

namespace App\Modules\Billing\Application\Listeners;

use App\Modules\Billing\Application\RecordUsage;
use App\Modules\StoredValue\Domain\Events\GiftCardIssued;
use Illuminate\Contracts\Queue\ShouldQueue;

class RecordGiftCardIssuedUsage implements ShouldQueue
{
    public string $queue = 'reporting';

    public function __construct(
        private readonly RecordUsage $recordUsage,
    ) {}

    public function handle(GiftCardIssued $event): void
    {
        $this->recordUsage->handle(
            merchantId: $event->merchantId,
            storeId: $event->storeId,
            metricKey: 'gift_cards.issued',
            quantity: 1,
            metadata: ['gift_card_id' => $event->giftCardId, 'amount_minor' => $event->amountMinor],
            sourceRef: $event->giftCardId,
        );
    }
}
