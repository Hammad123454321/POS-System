<?php

namespace App\Modules\Payments\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\OrderRegister\Domain\Models\CashMovement;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\OrderRegister\Domain\Models\Refund;
use App\Modules\Payments\Application\PaymentProviderRegistry;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Contracts\StoredValueLedger;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;

class RefundPayment
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
        private readonly PaymentProviderRegistry $paymentProviderRegistry,
        private readonly RecordUsage $recordUsage,
        private readonly StoredValueLedger $storedValueLedger,
    ) {}

    public function handle(Device $device, Payment $payment, ?int $amountMinor = null, ?string $reason = null): Refund
    {
        if ($payment->merchant_id !== $device->merchant_id || $payment->store_id !== $device->store_id) {
            throw new DomainException('The requested payment does not belong to this device context.');
        }

        $currentRefundableMinor = $this->currentRefundableMinor($payment);
        $requestedAmountMinor = $amountMinor ?? $currentRefundableMinor;

        if ($requestedAmountMinor <= 0) {
            throw new DomainException('Refund amount must be greater than zero.');
        }

        if ($requestedAmountMinor > $currentRefundableMinor) {
            throw new DomainException('Refund amount exceeds the remaining refundable balance.');
        }

        return DB::transaction(function () use ($currentRefundableMinor, $device, $payment, $reason, $requestedAmountMinor): Refund {
            $beforeState = $this->paymentSnapshot($payment);
            $refundedAt = CarbonImmutable::now('UTC');
            $providerRefundId = null;

            if ($payment->method === 'card') {
                $provider = $this->paymentProviderRegistry->forKey($payment->provider_key);
                $result = $provider->refund($device, $payment, $requestedAmountMinor, $reason);
                $providerRefundId = $result['provider_refund_id'] ?? null;
                $refundedAt = $result['refunded_at'] ?? $refundedAt;
            } elseif ($payment->method === 'cash') {
                CashMovement::query()->create([
                    'merchant_id' => $payment->merchant_id,
                    'store_id' => $payment->store_id,
                    'register_session_id' => $payment->register_session_id,
                    'device_id' => $device->id,
                    'type' => 'refund_cash',
                    'amount_minor' => -1 * $requestedAmountMinor,
                    'reference_type' => 'payment',
                    'reference_id' => $payment->id,
                    'metadata' => [
                        'reason' => $reason,
                    ],
                    'occurred_at' => $refundedAt,
                ]);

                $payment->order->registerSession()->firstOrFail()->decrement('expected_cash_minor', $requestedAmountMinor);
            } elseif ($payment->method === 'gift_card') {
                $giftCardCode = $payment->metadata['gift_card_code'] ?? null;

                if (! is_string($giftCardCode) || $giftCardCode === '') {
                    throw new DomainException('Gift card refund could not resolve the original stored value account.');
                }

                $this->storedValueLedger->credit(
                    $device,
                    $giftCardCode,
                    $requestedAmountMinor,
                    $payment->id,
                    $payment->order_id,
                    $reason,
                );
            } else {
                throw new DomainException('Refunds are not supported for this payment method.');
            }

            $remainingRefundableMinor = $currentRefundableMinor - $requestedAmountMinor;

            $payment->forceFill([
                'status' => $remainingRefundableMinor === 0 ? 'refunded' : 'partially_refunded',
                'refundable_minor' => $remainingRefundableMinor,
                'refunded_at' => $refundedAt,
            ])->save();

            $refund = Refund::query()->create([
                'merchant_id' => $payment->merchant_id,
                'store_id' => $payment->store_id,
                'order_id' => $payment->order_id,
                'payment_id' => $payment->id,
                'device_id' => $device->id,
                'amount_minor' => $requestedAmountMinor,
                'status' => $payment->status,
                'provider_refund_id' => $providerRefundId,
                'reason' => $reason,
                'metadata' => [
                    'method' => $payment->method,
                    'remaining_refundable_minor' => $remainingRefundableMinor,
                ],
                'refunded_at' => $refundedAt,
            ]);

            $this->auditLogger->log(
                $payment->merchant_id,
                $payment->store_id,
                'payments',
                'payment.refunded',
                'payment',
                $payment->id,
                $beforeState,
                $this->paymentSnapshot($payment),
                [
                    'refund_id' => $refund->id,
                    'amount_minor' => $requestedAmountMinor,
                    'reason' => $reason,
                ],
                null,
                $device->id,
            );
            $this->recordUsage->handle(
                $payment->merchant_id,
                $payment->store_id,
                'payment.refund.'.$payment->method,
            );

            return $refund->refresh();
        });
    }

    private function currentRefundableMinor(Payment $payment): int
    {
        if ($payment->refundable_minor > 0) {
            return (int) $payment->refundable_minor;
        }

        if ($payment->status === 'captured') {
            return (int) $payment->amount_minor;
        }

        return 0;
    }

    /**
     * @return array<string, mixed>
     */
    private function paymentSnapshot(Payment $payment): array
    {
        return [
            'id' => $payment->id,
            'method' => $payment->method,
            'status' => $payment->status,
            'amount_minor' => $payment->amount_minor,
            'refundable_minor' => $payment->refundable_minor,
            'refunded_at' => $payment->refunded_at?->toIso8601String(),
        ];
    }
}
