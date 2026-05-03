<?php

namespace App\Modules\Payments\Application\Providers;

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Application\Actions\PersistPaymentProviderEvent;
use App\Modules\Payments\Application\Providers\Fiserv\BluePayClient;
use App\Modules\Payments\Contracts\PaymentProvider;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;
use DomainException;

class FiservBluePayPaymentProvider implements PaymentProvider
{
    public function __construct(
        private readonly BluePayClient $bluePayClient,
        private readonly PersistPaymentProviderEvent $persistPaymentProviderEvent,
    ) {}

    public function key(): string
    {
        return 'fiserv_bluepay';
    }

    public function createIntent(Device $device, Order $order, array $tender): array
    {
        return [
            'provider_key' => $this->key(),
            'provider_transaction_id' => $tender['provider_transaction_id'] ?? null,
            'amount_minor' => (int) ($tender['amount_minor'] ?? 0),
            'tip_minor' => (int) ($tender['tip_minor'] ?? 0),
            'terminal_reference' => $tender['terminal_reference'] ?? null,
            'metadata' => $tender['metadata'] ?? [],
        ];
    }

    public function authorize(Device $device, Order $order, array $intent): array
    {
        $providerTransactionId = trim((string) ($intent['provider_transaction_id'] ?? ''));

        if ($providerTransactionId === '') {
            throw new DomainException('Terminal-approved card data is missing provider_transaction_id.');
        }

        return [
            'status' => 'authorized',
            'provider_key' => $this->key(),
            'provider_transaction_id' => $providerTransactionId,
            'authorized_minor' => (int) ($intent['amount_minor'] ?? 0),
            'tip_minor' => (int) ($intent['tip_minor'] ?? 0),
            'authorized_at' => CarbonImmutable::now('UTC'),
            'terminal_reference' => $intent['terminal_reference'] ?? null,
            'metadata' => $intent['metadata'] ?? [],
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
        $providerTransactionId = trim((string) $payment->provider_transaction_id);

        if ($providerTransactionId === '') {
            throw new DomainException('Card payment is missing provider transaction reference.');
        }

        $response = $this->bluePayClient->postTransaction([
            'TRANS_TYPE' => 'VOID',
            'MASTER_ID' => $providerTransactionId,
            'ORDER_ID' => $payment->order_id,
            'CUSTOM_ID' => $payment->merchant_id,
            'CUSTOM_ID2' => $payment->store_id,
            'AMOUNT' => $this->formatAmountMinor((int) $payment->amount_minor),
            ...($reason === null ? [] : ['MEMO' => $reason]),
        ]);
        $status = (string) ($response['STATUS'] ?? '');
        $isApproved = $status === '1';
        $message = trim((string) ($response['MESSAGE'] ?? ''));

        $this->persistPaymentProviderEvent->handle(
            providerKey: $this->key(),
            providerTransactionId: $providerTransactionId,
            eventType: 'bp20post.void',
            eventStatus: $status,
            providerAccountId: $response['ACCOUNT_ID'] ?? null,
            payment: $payment,
            payload: $response,
        );

        if (! $isApproved) {
            throw new DomainException(
                $message !== ''
                    ? 'Fiserv void failed: '.$message
                    : 'Fiserv void failed because the transaction is no longer voidable.',
            );
        }

        return [
            'status' => 'voided',
            'provider_void_id' => $response['TRANS_ID'] ?? null,
            'voided_at' => CarbonImmutable::now('UTC'),
            'provider_transaction_id' => $providerTransactionId,
        ];
    }

    public function refund(Device $device, Payment $payment, int $amountMinor, ?string $reason = null): array
    {
        $providerTransactionId = trim((string) $payment->provider_transaction_id);

        if ($providerTransactionId === '') {
            throw new DomainException('Card payment is missing provider transaction reference.');
        }

        $response = $this->bluePayClient->postTransaction([
            'TRANS_TYPE' => 'REFUND',
            'MASTER_ID' => $providerTransactionId,
            'ORDER_ID' => $payment->order_id,
            'CUSTOM_ID' => $payment->merchant_id,
            'CUSTOM_ID2' => $payment->store_id,
            'AMOUNT' => $this->formatAmountMinor($amountMinor),
            ...($reason === null ? [] : ['MEMO' => $reason]),
        ]);
        $status = (string) ($response['STATUS'] ?? '');
        $isApproved = $status === '1';
        $message = trim((string) ($response['MESSAGE'] ?? ''));

        $this->persistPaymentProviderEvent->handle(
            providerKey: $this->key(),
            providerTransactionId: $providerTransactionId,
            eventType: 'bp20post.refund',
            eventStatus: $status,
            providerAccountId: $response['ACCOUNT_ID'] ?? null,
            payment: $payment,
            payload: $response,
        );

        if (! $isApproved) {
            throw new DomainException(
                $message !== ''
                    ? 'Fiserv refund failed: '.$message
                    : 'Fiserv refund failed.',
            );
        }

        return [
            'status' => $amountMinor >= (int) $payment->refundable_minor ? 'refunded' : 'partially_refunded',
            'provider_refund_id' => $response['TRANS_ID'] ?? null,
            'refunded_minor' => $amountMinor,
            'refunded_at' => CarbonImmutable::now('UTC'),
        ];
    }

    public function reconcile(Device $device, array $providerTransactionIds): array
    {
        $results = [];

        foreach ($providerTransactionIds as $providerTransactionId) {
            $transactionId = trim((string) $providerTransactionId);

            if ($transactionId === '') {
                continue;
            }

            $inquiry = $this->inquire($device, $transactionId);
            $results[] = [
                'provider_transaction_id' => $transactionId,
                'status' => $inquiry['status'] ?? 'unknown',
                'trans_status' => $inquiry['trans_status'] ?? null,
                'trans_type' => $inquiry['trans_type'] ?? null,
                'settlement_id' => $inquiry['settlement_id'] ?? null,
                'settled_at' => $inquiry['settled_at'] ?? null,
                'raw' => $inquiry['raw'] ?? [],
            ];
        }

        return $results;
    }

    public function inquire(Device $device, string $providerTransactionId): array
    {
        $transactionId = trim($providerTransactionId);

        if ($transactionId === '') {
            throw new DomainException('Inquiry requires a provider transaction ID.');
        }

        $now = CarbonImmutable::now('UTC');
        $rows = $this->bluePayClient->fetchDailyReport([
            'REPORT_START_DATE' => $now->subDays(30)->format('Y-m-d H:i:s'),
            'REPORT_END_DATE' => $now->addDay()->format('Y-m-d H:i:s'),
            'TRANSACTION_ID' => $transactionId,
            'DO_NOT_ESCAPE' => '1',
            'RESPONSEVERSION' => '99',
        ]);
        $matched = collect($rows)->first(
            fn (array $row): bool => strtoupper((string) ($row['ID'] ?? '')) === strtoupper($transactionId),
        );

        if (! is_array($matched)) {
            $this->persistPaymentProviderEvent->handle(
                providerKey: $this->key(),
                providerTransactionId: $transactionId,
                eventType: 'daily_report.inquire',
                eventStatus: 'unknown',
                payload: ['rows' => []],
            );

            return [
                'provider_transaction_id' => $transactionId,
                'status' => 'unknown',
                'trans_status' => null,
                'trans_type' => null,
                'settlement_id' => null,
                'settled_at' => null,
                'raw' => [],
            ];
        }

        $status = $this->normalizeDailyReportStatus($matched);
        $transStatus = strtoupper((string) ($matched['STATUS'] ?? ''));
        $transType = strtoupper((string) ($matched['TRANS_TYPE'] ?? ''));
        $settlementId = trim((string) ($matched['SETTLEMENT_ID'] ?? ''));
        $settleDate = trim((string) ($matched['SETTLE_DATE'] ?? ''));

        $this->persistPaymentProviderEvent->handle(
            providerKey: $this->key(),
            providerTransactionId: $transactionId,
            eventType: 'daily_report.inquire',
            eventStatus: $transStatus,
            providerAccountId: $matched['ACCOUNT_ID'] ?? null,
            payment: $this->findPayment($device, $transactionId),
            payload: $matched,
        );

        return [
            'provider_transaction_id' => $transactionId,
            'status' => $status,
            'trans_status' => $transStatus,
            'trans_type' => $transType,
            'settlement_id' => $settlementId !== '' ? $settlementId : null,
            'settled_at' => $settleDate !== '' ? $settleDate : null,
            'raw' => $matched,
        ];
    }

    public function reverse(Device $device, string $providerTransactionId): array
    {
        $transactionId = trim($providerTransactionId);

        if ($transactionId === '') {
            throw new DomainException('Reverse requires a provider transaction ID.');
        }

        $response = $this->bluePayClient->postTransaction([
            'TRANS_TYPE' => 'VOID',
            'MASTER_ID' => $transactionId,
        ]);
        $status = (string) ($response['STATUS'] ?? '');
        $isApproved = $status === '1';

        $this->persistPaymentProviderEvent->handle(
            providerKey: $this->key(),
            providerTransactionId: $transactionId,
            eventType: 'bp20post.reverse',
            eventStatus: $status,
            providerAccountId: $response['ACCOUNT_ID'] ?? null,
            payment: $this->findPayment($device, $transactionId),
            payload: $response,
        );

        if (! $isApproved) {
            throw new DomainException('Fiserv reverse request failed.');
        }

        return [
            'provider_transaction_id' => $transactionId,
            'status' => 'reversed',
            'reversed_at' => CarbonImmutable::now('UTC'),
        ];
    }

    public function getTerminalCapabilities(Device $device): array
    {
        return [
            'provider_key' => $this->key(),
            'acquirer' => 'first_data_fiserv',
            'integration_mode' => 'semi_integrated',
            'capture_mode' => 'immediate',
            'supports_tip_adjust' => false,
            'supports_partial_capture' => false,
            'supports_refund' => true,
            'supports_void' => true,
        ];
    }

    private function formatAmountMinor(int $amountMinor): string
    {
        return number_format($amountMinor / 100, 2, '.', '');
    }

    /**
     * @param  array<string, string>  $row
     */
    private function normalizeDailyReportStatus(array $row): string
    {
        $transStatus = strtoupper((string) ($row['STATUS'] ?? ''));
        $transType = strtoupper((string) ($row['TRANS_TYPE'] ?? ''));
        $isVoid = ((string) ($row['F_VOID'] ?? '')) === '1' || $transType === 'VOID';

        if ($transStatus !== '1') {
            return 'unknown';
        }

        if ($isVoid) {
            return 'voided';
        }

        if ($transType === 'REFUND') {
            return 'refunded';
        }

        return 'captured';
    }

    private function findPayment(Device $device, string $providerTransactionId): ?Payment
    {
        return Payment::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('provider_key', $this->key())
            ->where('provider_transaction_id', $providerTransactionId)
            ->first();
    }
}
