<?php

namespace App\Modules\DeliveryIntegrations\Domain\Events;

/**
 * Emitted (after commit) when an external delivery order is ingested. Carries
 * IDs only — listeners are queued and must not serialize Eloquent models.
 */
class DeliveryOrderIngested
{
    public function __construct(
        public readonly string $externalOrderLinkId,
        public readonly ?string $merchantId,
        public readonly ?string $storeId,
        public readonly string $channelKey,
    ) {}
}
