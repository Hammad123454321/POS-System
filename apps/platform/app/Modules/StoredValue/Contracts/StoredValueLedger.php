<?php

namespace App\Modules\StoredValue\Contracts;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\Store;

interface StoredValueLedger
{
    /**
     * @return array<string, mixed>
     */
    public function issue(Store $store, ?string $customerId, int $amountMinor, ?string $requestedCode = null): array;

    /**
     * @return array<string, mixed>
     */
    public function topUp(Device $device, string $giftCardCode, int $amountMinor): array;

    /**
     * @return array<string, mixed>
     */
    public function balance(string $merchantId, string $giftCardCode): array;

    /**
     * @return array<string, mixed>
     */
    public function createHold(Device $device, string $giftCardCode, int $amountMinor, ?string $orderId = null): array;

    /**
     * @return array<string, mixed>
     */
    public function captureHold(string $holdId, ?string $paymentId = null): array;

    /**
     * @return array<string, mixed>
     */
    public function releaseHold(string $holdId): array;

    /**
     * @return array<string, mixed>
     */
    public function credit(
        Device $device,
        string $giftCardCode,
        int $amountMinor,
        ?string $paymentId = null,
        ?string $orderId = null,
        ?string $reason = null,
    ): array;
}
