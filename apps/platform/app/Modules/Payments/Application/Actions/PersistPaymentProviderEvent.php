<?php

namespace App\Modules\Payments\Application\Actions;

use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Domain\Models\PaymentProviderEvent;
use Carbon\CarbonImmutable;

class PersistPaymentProviderEvent
{
    /**
     * @param  array<string, mixed>|null  $payload
     */
    public function handle(
        string $providerKey,
        string $providerTransactionId,
        string $eventType,
        ?string $eventStatus = null,
        ?string $providerAccountId = null,
        ?Payment $payment = null,
        ?bool $signatureValid = null,
        ?array $payload = null,
    ): PaymentProviderEvent {
        return PaymentProviderEvent::query()->updateOrCreate(
            [
                'provider_key' => $providerKey,
                'provider_transaction_id' => $providerTransactionId,
                'event_type' => $eventType,
            ],
            [
                'merchant_id' => $payment?->merchant_id,
                'store_id' => $payment?->store_id,
                'payment_id' => $payment?->id,
                'provider_account_id' => $providerAccountId,
                'event_status' => $eventStatus,
                'signature_valid' => $signatureValid,
                'payload' => $payload,
                'occurred_at' => CarbonImmutable::now('UTC'),
            ],
        );
    }
}
