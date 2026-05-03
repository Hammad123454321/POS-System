<?php

namespace App\Modules\StoredValue\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Contracts\StoredValueLedger;

class LookupGiftCardForDevice
{
    public function __construct(
        private readonly StoredValueLedger $storedValueLedger,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, string $giftCardCode): array
    {
        return $this->storedValueLedger->balance($device->merchant_id, $giftCardCode);
    }
}
