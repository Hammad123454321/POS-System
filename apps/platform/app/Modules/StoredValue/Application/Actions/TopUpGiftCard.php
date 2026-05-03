<?php

namespace App\Modules\StoredValue\Application\Actions;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Contracts\StoredValueLedger;

class TopUpGiftCard
{
    public function __construct(
        private readonly StoredValueLedger $storedValueLedger,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, string $giftCardCode, int $amountMinor): array
    {
        return $this->storedValueLedger->topUp($device, $giftCardCode, $amountMinor);
    }
}
