<?php

namespace App\Modules\PlatformCore\Application\DeviceAuth;

use RuntimeException;

class DeviceAuthException extends RuntimeException
{
    public function __construct(
        string $message,
        public readonly int $status,
        public readonly string $errorCode,
    ) {
        parent::__construct($message);
    }
}
