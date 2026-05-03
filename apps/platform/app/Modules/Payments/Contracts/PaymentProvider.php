<?php

namespace App\Modules\Payments\Contracts;

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\PlatformCore\Domain\Models\Device;

interface PaymentProvider
{
    public function key(): string;

    /**
     * @param  array<string, mixed>  $tender
     * @return array<string, mixed>
     */
    public function createIntent(Device $device, Order $order, array $tender): array;

    /**
     * @param  array<string, mixed>  $intent
     * @return array<string, mixed>
     */
    public function authorize(Device $device, Order $order, array $intent): array;

    /**
     * @return array<string, mixed>
     */
    public function capture(Device $device, Payment $payment, int $amountMinor): array;

    /**
     * @return array<string, mixed>
     */
    public function void(Device $device, Payment $payment, ?string $reason = null): array;

    /**
     * @return array<string, mixed>
     */
    public function refund(Device $device, Payment $payment, int $amountMinor, ?string $reason = null): array;

    /**
     * @param  array<int, string>  $providerTransactionIds
     * @return array<int, array<string, mixed>>
     */
    public function reconcile(Device $device, array $providerTransactionIds): array;

    /**
     * @return array<string, mixed>
     */
    public function inquire(Device $device, string $providerTransactionId): array;

    /**
     * @return array<string, mixed>
     */
    public function reverse(Device $device, string $providerTransactionId): array;

    /**
     * @return array<string, mixed>
     */
    public function getTerminalCapabilities(Device $device): array;
}
