<?php

namespace App\Modules\OrderRegister\Domain\Events;

/**
 * Emitted (after commit) when an order reaches a paid terminal state. Carries
 * IDs only — listeners are queued and must not serialize Eloquent models.
 */
class OrderPaid
{
    public function __construct(
        public readonly string $orderId,
        public readonly ?string $merchantId,
        public readonly ?string $storeId,
        public readonly int $totalMinor,
    ) {}
}
