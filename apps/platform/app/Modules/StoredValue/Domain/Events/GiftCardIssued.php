<?php

namespace App\Modules\StoredValue\Domain\Events;

/**
 * Emitted (after commit) when a gift card is issued. Carries IDs/amounts only —
 * listeners are queued and must not serialize Eloquent models.
 */
class GiftCardIssued
{
    public function __construct(
        public readonly string $giftCardId,
        public readonly ?string $merchantId,
        public readonly ?string $storeId,
        public readonly int $amountMinor,
    ) {}
}
