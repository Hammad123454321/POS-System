<?php

namespace App\Modules\Payments\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\OrderRegister\Domain\Models\VoidRecord;
use App\Modules\Payments\Application\PaymentProviderRegistry;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;

class VoidPayment
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
        private readonly PaymentProviderRegistry $paymentProviderRegistry,
        private readonly RecordUsage $recordUsage,
    ) {}

    public function handle(Device $device, Payment $payment, ?string $reason = null): VoidRecord
    {
        if ($payment->merchant_id !== $device->merchant_id || $payment->store_id !== $device->store_id) {
            throw new DomainException('The requested payment does not belong to this device context.');
        }

        if ($payment->method !== 'card') {
            throw new DomainException('Only card payments may be voided.');
        }

        if (! in_array($payment->status, ['captured', 'authorized'], true)) {
            throw new DomainException('Only unsettled card payments may be voided.');
        }

        return DB::transaction(function () use ($device, $payment, $reason): VoidRecord {
            $beforeState = $this->paymentSnapshot($payment);
            $provider = $this->paymentProviderRegistry->forKey($payment->provider_key);
            $result = $provider->void($device, $payment, $reason);
            $voidedAt = $result['voided_at'] ?? CarbonImmutable::now('UTC');

            $payment->forceFill([
                'status' => $result['status'] ?? 'voided',
                'voided_at' => $voidedAt,
                'refundable_minor' => 0,
            ])->save();

            $void = VoidRecord::query()->create([
                'merchant_id' => $payment->merchant_id,
                'store_id' => $payment->store_id,
                'order_id' => $payment->order_id,
                'payment_id' => $payment->id,
                'device_id' => $device->id,
                'status' => $payment->status,
                'provider_void_id' => $result['provider_void_id'] ?? null,
                'reason' => $reason,
                'metadata' => [
                    'method' => $payment->method,
                ],
                'voided_at' => $voidedAt,
            ]);

            $this->auditLogger->log(
                $payment->merchant_id,
                $payment->store_id,
                'payments',
                'payment.voided',
                'payment',
                $payment->id,
                $beforeState,
                $this->paymentSnapshot($payment),
                [
                    'void_record_id' => $void->id,
                    'reason' => $reason,
                ],
                null,
                $device->id,
            );
            $this->recordUsage->handle(
                $payment->merchant_id,
                $payment->store_id,
                'payment.void.card',
            );

            return $void->refresh();
        });
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
            'authorized_at' => $payment->authorized_at?->toIso8601String(),
            'voided_at' => $payment->voided_at?->toIso8601String(),
        ];
    }
}
