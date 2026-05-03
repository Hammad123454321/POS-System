<?php

namespace App\Modules\Payments\Application\Actions;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Application\Providers\Fiserv\BluePayCrypto;
use Carbon\CarbonImmutable;
use Illuminate\Support\Arr;

class ProcessFiservTransNotifyWebhook
{
    public function __construct(
        private readonly BluePayCrypto $bluePayCrypto,
        private readonly OpenExceptionCase $openExceptionCase,
        private readonly PersistPaymentProviderEvent $persistPaymentProviderEvent,
    ) {}

    /**
     * @param  array<string, string>  $payload
     * @return array{signature_valid: bool, duplicate: bool, matched_payment_id: string|null, provider_transaction_id: string|null}
     */
    public function handle(array $payload): array
    {
        $normalizedPayload = $this->normalizePayload($payload);
        $providerTransactionId = trim((string) ($normalizedPayload['TRANS_ID'] ?? ''));
        $providerAccountId = trim((string) ($normalizedPayload['ACCOUNT_ID'] ?? ''));
        $hashType = trim((string) ($normalizedPayload['TPS_HASH_TYPE'] ?? config('pos.payments.fiserv_bluepay.hash_type', 'HMAC_SHA256')));
        $bpStamp = trim((string) ($normalizedPayload['BP_STAMP'] ?? ''));
        $bpStampDef = trim((string) ($normalizedPayload['BP_STAMP_DEF'] ?? 'trans_id trans_status trans_type amount batch_id batch_status total_count total_amount bupload_id rebill_id reb_amount status'));
        $signatureValid = $this->verifySignature($normalizedPayload, $hashType, $bpStampDef, $bpStamp);
        $transType = strtoupper((string) ($normalizedPayload['TRANS_TYPE'] ?? ''));
        $transStatus = strtoupper((string) ($normalizedPayload['TRANS_STATUS'] ?? $normalizedPayload['STATUS'] ?? ''));
        $eventType = 'trans_notify.'.strtolower($transType !== '' ? $transType : 'unknown');
        $eventStatus = $transStatus !== '' ? $transStatus : null;

        $payments = $providerTransactionId === ''
            ? collect()
            : Payment::query()
                ->where('provider_key', 'fiserv_bluepay')
                ->where('provider_transaction_id', $providerTransactionId)
                ->get();
        $matchedPayment = $payments->count() === 1 ? $payments->first() : null;

        $this->persistPaymentProviderEvent->handle(
            providerKey: 'fiserv_bluepay',
            providerTransactionId: $providerTransactionId !== '' ? $providerTransactionId : 'unknown',
            eventType: $eventType,
            eventStatus: $eventStatus,
            providerAccountId: $providerAccountId !== '' ? $providerAccountId : null,
            payment: $matchedPayment,
            signatureValid: $signatureValid,
            payload: $normalizedPayload,
        );

        if (! $signatureValid) {
            $this->ensureExceptionCase(
                module: 'payments',
                code: 'webhook_signature_failure',
                severity: 'high',
                title: 'Fiserv webhook signature verification failed.',
                relatedType: 'payment_provider_event',
                relatedId: $providerTransactionId !== '' ? $providerTransactionId : null,
                details: [
                    'provider_key' => 'fiserv_bluepay',
                    'provider_transaction_id' => $providerTransactionId !== '' ? $providerTransactionId : null,
                    'provider_account_id' => $providerAccountId !== '' ? $providerAccountId : null,
                    'event_type' => $eventType,
                ],
            );

            return [
                'signature_valid' => false,
                'duplicate' => false,
                'matched_payment_id' => null,
                'provider_transaction_id' => $providerTransactionId !== '' ? $providerTransactionId : null,
            ];
        }

        if ($providerTransactionId === '' || $payments->isEmpty()) {
            $this->ensureExceptionCase(
                module: 'payments',
                code: 'unmatched_provider_transaction',
                severity: 'medium',
                title: 'Fiserv webhook transaction could not be matched to a payment.',
                relatedType: 'payment_provider_event',
                relatedId: $providerTransactionId !== '' ? $providerTransactionId : null,
                details: [
                    'provider_key' => 'fiserv_bluepay',
                    'provider_transaction_id' => $providerTransactionId !== '' ? $providerTransactionId : null,
                    'provider_account_id' => $providerAccountId !== '' ? $providerAccountId : null,
                    'event_type' => $eventType,
                ],
            );
        } elseif ($payments->count() > 1) {
            $this->ensureExceptionCase(
                module: 'payments',
                code: 'duplicate_provider_transaction_id',
                severity: 'high',
                title: 'Multiple payments share the same provider transaction ID.',
                relatedType: 'payment_provider_event',
                relatedId: $providerTransactionId,
                details: [
                    'provider_key' => 'fiserv_bluepay',
                    'provider_transaction_id' => $providerTransactionId,
                    'payment_ids' => $payments->pluck('id')->all(),
                ],
            );
        } elseif ($matchedPayment !== null) {
            $this->applyPaymentUpdateFromWebhook($matchedPayment, $normalizedPayload);
        }

        return [
            'signature_valid' => true,
            'duplicate' => $payments->count() > 1,
            'matched_payment_id' => $matchedPayment?->id,
            'provider_transaction_id' => $providerTransactionId !== '' ? $providerTransactionId : null,
        ];
    }

    /**
     * @param  array<string, string>  $payload
     */
    private function verifySignature(array $payload, string $hashType, string $definition, string $bpStamp): bool
    {
        if (! filter_var(config('pos.payments.fiserv_bluepay.webhook.verify_bp_stamp', true), FILTER_VALIDATE_BOOL)) {
            return true;
        }

        if ($bpStamp === '') {
            return false;
        }

        $secretKey = (string) config('pos.payments.fiserv_bluepay.secret_key', '');

        if (trim($secretKey) === '') {
            return false;
        }

        $stampFields = [];

        foreach ($payload as $key => $value) {
            $stampFields[$key] = $value;
            $stampFields[strtoupper($key)] = $value;
            $stampFields[strtolower($key)] = $value;
        }

        return $this->bluePayCrypto->verifyStamp(
            secretKey: $secretKey,
            hashType: $hashType,
            definition: $definition,
            fields: $stampFields,
            providedStamp: $bpStamp,
        );
    }

    /**
     * @param  array<string, string>  $payload
     */
    private function applyPaymentUpdateFromWebhook(Payment $payment, array $payload): void
    {
        $transType = strtoupper((string) ($payload['TRANS_TYPE'] ?? ''));
        $transStatus = strtoupper((string) ($payload['TRANS_STATUS'] ?? $payload['STATUS'] ?? ''));
        $isVoid = ((string) ($payload['F_VOID'] ?? '')) === '1' || $transType === 'VOID';
        $issueDate = trim((string) ($payload['ISSUE_DATE'] ?? ''));
        $occurredAt = $issueDate !== ''
            ? CarbonImmutable::createFromFormat('Y-m-d H:i:s', $issueDate, 'UTC') ?: CarbonImmutable::now('UTC')
            : CarbonImmutable::now('UTC');

        if ($transStatus === '1' && $isVoid) {
            $payment->forceFill([
                'status' => 'voided',
                'voided_at' => $occurredAt,
                'refundable_minor' => 0,
                'metadata' => [
                    ...($payment->metadata ?? []),
                    'last_webhook_event' => Arr::only($payload, ['TRANS_ID', 'TRANS_TYPE', 'TRANS_STATUS', 'ISSUE_DATE']),
                ],
            ])->save();

            return;
        }

        if ($transStatus === '1' && $payment->status === 'in_doubt') {
            $payment->forceFill([
                'status' => 'captured',
                'captured_at' => $occurredAt,
                'refundable_minor' => max(0, (int) $payment->amount_minor),
                'metadata' => [
                    ...($payment->metadata ?? []),
                    'last_webhook_event' => Arr::only($payload, ['TRANS_ID', 'TRANS_TYPE', 'TRANS_STATUS', 'ISSUE_DATE']),
                ],
            ])->save();
        }
    }

    /**
     * @param  array<string, string>  payload
     * @return array<string, string>
     */
    private function normalizePayload(array $payload): array
    {
        $normalized = [];

        foreach ($payload as $key => $value) {
            $normalized[strtoupper((string) $key)] = (string) $value;
        }

        return $normalized;
    }

    /**
     * @param  array<string, mixed>  $details
     */
    private function ensureExceptionCase(
        string $module,
        string $code,
        string $severity,
        string $title,
        ?string $relatedType,
        ?string $relatedId,
        array $details,
    ): void {
        $existingCase = ExceptionCase::query()
            ->where('module', $module)
            ->where('code', $code)
            ->where('status', 'open')
            ->where('related_type', $relatedType)
            ->where('related_id', $relatedId)
            ->first();

        if ($existingCase !== null) {
            return;
        }

        $this->openExceptionCase->handle(
            merchantId: null,
            storeId: null,
            module: $module,
            code: $code,
            severity: $severity,
            title: $title,
            details: $details,
            relatedType: $relatedType,
            relatedId: $relatedId,
            openedByDeviceId: null,
            openedByUserId: null,
        );
    }
}
