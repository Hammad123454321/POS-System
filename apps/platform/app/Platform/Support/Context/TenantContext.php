<?php

namespace App\Platform\Support\Context;

final class TenantContext
{
    public function __construct(
        public readonly ?string $merchantId,
        public readonly ?string $storeId = null,
        public readonly ?string $deviceId = null,
    ) {}

    public function isResolved(): bool
    {
        return $this->merchantId !== null;
    }
}
