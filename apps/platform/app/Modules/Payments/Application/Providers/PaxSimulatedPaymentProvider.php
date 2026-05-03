<?php

namespace App\Modules\Payments\Application\Providers;

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Contracts\PaymentProvider;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Str;

class PaxSimulatedPaymentProvider implements PaymentProvider
{
    public function key(): string
    {
        return 'pax_simulator';
    }

    public function createIntent(Device $device, Order $order, array $tender): array
    {
        return [
            'provider_key' => $this->key(),
            'provider_transaction_id' => 'PAX-'.Str::upper(Str::random(18)),
            'terminal_reference' => $tender['terminal_reference'] ?? 'PAX-'.$device->id,
            'amount_minor' => (int) $tender['amount_minor'],
            'tip_minor' => (int) ($tender['tip_minor'] ?? 0),
        ];
    }

    public function authorize(Device $device, Order $order, array $intent): array
    {
        $authorizedAt = CarbonImmutable::now('UTC');

        return [
            'status' => 'authorized',
            'provider_key' => $this->key(),
            'provider_transaction_id' => $intent['provider_transaction_id'],
            'terminal_reference' => $intent['terminal_reference'],
            'authorized_minor' => (int) $intent['amount_minor'],
            'tip_minor' => (int) $intent['tip_minor'],
            'authorized_at' => $authorizedAt,
            'metadata' => [
                'masked_pan' => '************4242',
                'auth_code' => Str::upper(Str::random(6)),
                'terminal_id' => $intent['terminal_reference'],
                'application_label' => 'VISA CREDIT',
                'aid' => 'A0000000031010',
                'tvr' => '0000008000',
                'tsi' => 'E800',
                'entry_mode' => 'chip',
                'transaction_reference' => $intent['provider_transaction_id'],
            ],
        ];
    }

    public function capture(Device $device, Payment $payment, int $amountMinor): array
    {
        return [
            'status' => 'captured',
            'provider_transaction_id' => $payment->provider_transaction_id,
            'captured_minor' => $amountMinor,
            'captured_at' => CarbonImmutable::now('UTC'),
        ];
    }

    public function void(Device $device, Payment $payment, ?string $reason = null): array
    {
        if ($payment->status !== 'authorized') {
            throw new DomainException('Only authorized card payments may be voided.');
        }

        return [
            'status' => 'voided',
            'provider_void_id' => 'VOID-'.Str::upper(Str::random(18)),
            'voided_at' => CarbonImmutable::now('UTC'),
            'reason' => $reason,
        ];
    }

    public function refund(Device $device, Payment $payment, int $amountMinor, ?string $reason = null): array
    {
        if (! in_array($payment->status, ['captured', 'partially_refunded', 'refunded'], true)) {
            throw new DomainException('Only captured payments may be refunded.');
        }

        return [
            'status' => $amountMinor >= $payment->refundable_minor ? 'refunded' : 'partially_refunded',
            'provider_refund_id' => 'RFN-'.Str::upper(Str::random(18)),
            'refunded_minor' => $amountMinor,
            'refunded_at' => CarbonImmutable::now('UTC'),
            'reason' => $reason,
        ];
    }

    public function reconcile(Device $device, array $providerTransactionIds): array
    {
        return collect($providerTransactionIds)->map(fn (string $providerTransactionId): array => [
            'provider_transaction_id' => $providerTransactionId,
            'status' => 'captured',
        ])->values()->all();
    }

    public function inquire(Device $device, string $providerTransactionId): array
    {
        /** @var Payment|null $payment */
        $payment = Payment::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('provider_transaction_id', $providerTransactionId)
            ->first();

        return [
            'provider_transaction_id' => $providerTransactionId,
            'status' => $payment?->status ?? 'unknown',
        ];
    }

    public function reverse(Device $device, string $providerTransactionId): array
    {
        return [
            'provider_transaction_id' => $providerTransactionId,
            'status' => 'reversed',
            'reversed_at' => CarbonImmutable::now('UTC'),
        ];
    }

    public function getTerminalCapabilities(Device $device): array
    {
        return [
            'provider_key' => $this->key(),
            'integration_mode' => 'semi_integrated',
            'supports_tip_adjust' => (bool) config('pos.payments.terminal_tip_adjust_supported', false),
            'supports_partial_capture' => false,
            'supports_refund' => true,
            'supports_void' => true,
        ];
    }
}
