<?php

namespace App\Modules\PlatformCore\Application\Concurrency;

use RuntimeException;

class EditLeaseException extends RuntimeException
{
    public function __construct(
        string $message,
        public readonly string $errorCode,
        public readonly int $status,
        public readonly ?int $leaseVersion = null,
        public readonly ?string $currentHolderDeviceId = null,
        public readonly ?string $leaseExpiredAt = null,
        public readonly ?string $leaseExpiresAt = null,
    ) {
        parent::__construct($message);
    }
}
