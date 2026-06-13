<?php

namespace App\Modules\Payments\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Events\OrderPaid;
use App\Modules\OrderRegister\Domain\Models\CashMovement;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\OrderRegister\Domain\Models\PaymentSplit;
use App\Modules\OrderRegister\Domain\Models\Receipt;
use App\Modules\Payments\Application\Exceptions\CardInDoubtException;
use App\Modules\Payments\Application\PaymentProviderRegistry;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Contracts\StoredValueLedger;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\Str;

class TenderOrder
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
        private readonly PaymentProviderRegistry $paymentProviderRegistry,
        private readonly RecordUsage $recordUsage,
        private readonly StoredValueLedger $storedValueLedger,
    ) {}

    /**
     * @param  array<int, array{
     *  method: string,
     *  amount_minor: int,
     *  tip_minor?: int,
     *  tendered_minor?: int|null,
     *  gift_card_code?: string|null,
     *  provider_key?: string|null,
     *  provider_transaction_id?: string|null,
     *  terminal_reference?: string|null,
     *  auth_code?: string|null,
     *  masked_pan?: string|null,
     *  terminal_id?: string|null,
     *  entry_mode?: string|null,
     *  application_label?: string|null,
     *  aid?: string|null,
     *  tvr?: string|null,
     *  tsi?: string|null,
     *  terminal_status_code?: string|null,
     *  terminal_result_code?: string|null,
     *  terminal_timestamp?: string|null
     * } >  $tenders
     */
    public function handle(Device $device, Order $order, array $tenders): Receipt
    {
        if ($order->status !== 'open') {
            throw new DomainException('Only open orders can be tendered.');
        }

        if ($order->merchant_id !== $device->merchant_id || $order->store_id !== $device->store_id) {
            throw new DomainException('The requested order does not belong to this device context.');
        }

        if ($tenders === []) {
            throw new DomainException('At least one tender is required.');
        }

        if ($this->hasOpenCardInDoubtException($order)) {
            throw new DomainException('This order has an unresolved in-doubt card transaction. Complete inquiry/reconciliation before retrying.');
        }

        $appliedMinor = 0;
        $tipMinor = 0;

        foreach ($tenders as $tender) {
            $appliedMinor += (int) $tender['amount_minor'];
            $tipMinor += (int) ($tender['tip_minor'] ?? 0);
        }

        if ($appliedMinor !== (int) $order->total_minor) {
            throw new DomainException('Tender amounts must exactly match the order total before tip.');
        }

        return DB::transaction(function () use ($device, $order, $tenders, $tipMinor): Receipt {
            $capturedAt = CarbonImmutable::now('UTC');
            $payments = [];
            $cashCapturedMinor = 0;

            foreach ($tenders as $index => $tender) {
                $payments[] = match ($tender['method']) {
                    'cash' => $this->captureCashTender($device, $order, $tender, $capturedAt, $cashCapturedMinor),
                    'card' => $this->captureCardTender($device, $order, $tender, $capturedAt),
                    'gift_card' => $this->captureGiftCardTender($device, $order, $tender, $capturedAt),
                    default => throw new DomainException('The requested tender method is not supported.'),
                };

                $payment = $payments[array_key_last($payments)];

                PaymentSplit::query()->create([
                    'merchant_id' => $order->merchant_id,
                    'store_id' => $order->store_id,
                    'order_id' => $order->id,
                    'payment_id' => $payment->id,
                    'split_sequence' => $index + 1,
                    'method' => $payment->method,
                    'applied_minor' => $payment->applied_minor,
                    'tip_minor' => $payment->tip_minor,
                    'status' => $payment->status,
                    'metadata' => [
                        'provider_key' => $payment->provider_key,
                        'provider_transaction_id' => $payment->provider_transaction_id,
                    ],
                ]);
            }

            if ($cashCapturedMinor > 0) {
                $order->registerSession()->firstOrFail()->increment('expected_cash_minor', $cashCapturedMinor);
            }

            $order->forceFill([
                'status' => 'paid',
                'tip_minor' => $tipMinor,
                'paid_minor' => (int) $order->total_minor + $tipMinor,
                'closed_at' => $capturedAt,
            ])->save();

            // Meter the paid order via a domain event after the tx commits, so
            // billing/usage runs out-of-band on the reporting queue.
            $paidTotal = (int) $order->paid_minor;
            DB::afterCommit(fn () => Event::dispatch(new OrderPaid(
                orderId: $order->id,
                merchantId: $order->merchant_id,
                storeId: $order->store_id,
                totalMinor: $paidTotal,
            )));

            $order->loadMissing('lines', 'customer');

            $receipt = Receipt::query()->create([
                'merchant_id' => $order->merchant_id,
                'store_id' => $order->store_id,
                'order_id' => $order->id,
                'payment_id' => count($payments) === 1 ? $payments[0]->id : null,
                'register_session_id' => $order->register_session_id,
                'device_id' => $device->id,
                'receipt_number' => 'RCP-'.Str::upper(Str::random(10)),
                'printed_at' => null,
                'payload' => $this->buildReceiptPayload($order, $payments, $capturedAt),
            ]);

            $this->auditLogger->log(
                $order->merchant_id,
                $order->store_id,
                'payments',
                'order.tendered',
                'order',
                $order->id,
                null,
                [
                    'status' => $order->status,
                    'paid_minor' => $order->paid_minor,
                    'tip_minor' => $order->tip_minor,
                    'receipt_id' => $receipt->id,
                ],
                [
                    'payment_ids' => array_map(fn (Payment $payment): string => $payment->id, $payments),
                ],
                null,
                $device->id,
            );

            return $receipt->refresh();
        });
    }

    /**
     * @param  array{method: string, amount_minor: int, tip_minor?: int, tendered_minor?: int|null}  $tender
     */
    private function captureCashTender(
        Device $device,
        Order $order,
        array $tender,
        CarbonImmutable $capturedAt,
        int &$cashCapturedMinor,
    ): Payment {
        $appliedMinor = (int) $tender['amount_minor'];
        $tipMinor = (int) ($tender['tip_minor'] ?? 0);
        $capturedMinor = $appliedMinor + $tipMinor;
        $tenderedMinor = (int) ($tender['tendered_minor'] ?? $capturedMinor);

        if ($tenderedMinor < $capturedMinor) {
            throw new DomainException('Tendered cash is below the requested applied amount.');
        }

        $payment = Payment::query()->create([
            'merchant_id' => $order->merchant_id,
            'store_id' => $order->store_id,
            'order_id' => $order->id,
            'register_session_id' => $order->register_session_id,
            'device_id' => $device->id,
            'method' => 'cash',
            'status' => 'captured',
            'amount_minor' => $capturedMinor,
            'applied_minor' => $appliedMinor,
            'tip_minor' => $tipMinor,
            'refundable_minor' => $capturedMinor,
            'tendered_minor' => $tenderedMinor,
            'change_minor' => $tenderedMinor - $capturedMinor,
            'captured_at' => $capturedAt,
            'metadata' => [
                'change_minor' => $tenderedMinor - $capturedMinor,
            ],
        ]);

        CashMovement::query()->create([
            'merchant_id' => $order->merchant_id,
            'store_id' => $order->store_id,
            'register_session_id' => $order->register_session_id,
            'device_id' => $device->id,
            'type' => 'sale_cash',
            'amount_minor' => $capturedMinor,
            'reference_type' => 'order',
            'reference_id' => $order->id,
            'occurred_at' => $capturedAt,
        ]);

        $cashCapturedMinor += $capturedMinor;

        $this->auditLogger->log(
            $order->merchant_id,
            $order->store_id,
            'payments',
            'payment.captured',
            'payment',
            $payment->id,
            null,
            $this->paymentSnapshot($payment),
            [
                'method' => 'cash',
            ],
            null,
            $device->id,
        );
        $this->recordUsage->handle(
            $order->merchant_id,
            $order->store_id,
            'payment.capture.cash',
        );

        return $payment;
    }

    /**
     * @param  array{method: string, amount_minor: int, tip_minor?: int, terminal_reference?: string|null}  $tender
     */
    private function captureCardTender(
        Device $device,
        Order $order,
        array $tender,
        CarbonImmutable $capturedAt,
    ): Payment {
        $appliedMinor = (int) $tender['amount_minor'];
        $tipMinor = (int) ($tender['tip_minor'] ?? 0);
        $capturedMinor = $appliedMinor + $tipMinor;
        $terminalStatusCode = Str::lower(trim((string) ($tender['terminal_status_code'] ?? '')));
        $terminalResultCode = Str::lower(trim((string) ($tender['terminal_result_code'] ?? '')));

        if ($this->isInDoubtTerminalResult($terminalStatusCode, $terminalResultCode)) {
            $this->recordUsage->handle(
                $order->merchant_id,
                $order->store_id,
                'payment.card.in_doubt',
            );
            if (
                in_array($terminalStatusCode, ['timeout', 'no_response'], true)
                || in_array($terminalResultCode, ['timeout', 'no_response'], true)
            ) {
                $this->recordUsage->handle(
                    $order->merchant_id,
                    $order->store_id,
                    'payment.terminal.timeout',
                );
            }

            throw new CardInDoubtException([
                'order_id' => $order->id,
                'provider_transaction_id' => $tender['provider_transaction_id'] ?? null,
                'terminal_id' => $tender['terminal_id'] ?? null,
                'terminal_status_code' => $tender['terminal_status_code'] ?? null,
                'terminal_result_code' => $tender['terminal_result_code'] ?? null,
            ]);
        }

        if (! $this->isApprovedTerminalResult($terminalStatusCode, $terminalResultCode)) {
            throw new DomainException('Card terminal declined. The order was not tendered.');
        }

        $provider = $this->paymentProviderRegistry->forKey(
            is_string($tender['provider_key'] ?? null) ? $tender['provider_key'] : null,
        );
        $providerTransactionId = trim((string) ($tender['provider_transaction_id'] ?? ''));

        if ($providerTransactionId === '') {
            throw new DomainException('Card tenders require provider_transaction_id from terminal approval.');
        }

        $capturedAtForPayment = $this->parseTerminalTimestamp(
            is_string($tender['terminal_timestamp'] ?? null) ? $tender['terminal_timestamp'] : null,
            $capturedAt,
        );
        $metadata = [
            'masked_pan' => $tender['masked_pan'] ?? null,
            'auth_code' => $tender['auth_code'] ?? null,
            'terminal_id' => $tender['terminal_id'] ?? null,
            'application_label' => $tender['application_label'] ?? null,
            'aid' => $tender['aid'] ?? null,
            'tvr' => $tender['tvr'] ?? null,
            'tsi' => $tender['tsi'] ?? null,
            'entry_mode' => $tender['entry_mode'] ?? null,
            'terminal_status_code' => $tender['terminal_status_code'] ?? null,
            'terminal_result_code' => $tender['terminal_result_code'] ?? null,
            'terminal_timestamp' => $capturedAtForPayment->toIso8601String(),
            'transaction_reference' => $providerTransactionId,
        ];

        $payment = Payment::query()->create([
            'merchant_id' => $order->merchant_id,
            'store_id' => $order->store_id,
            'order_id' => $order->id,
            'register_session_id' => $order->register_session_id,
            'device_id' => $device->id,
            'method' => 'card',
            'status' => 'captured',
            'provider_key' => $provider->key(),
            'provider_transaction_id' => $providerTransactionId,
            'terminal_reference' => is_string($tender['terminal_reference'] ?? null) ? $tender['terminal_reference'] : null,
            'amount_minor' => $capturedMinor,
            'applied_minor' => $appliedMinor,
            'tip_minor' => $tipMinor,
            'refundable_minor' => $capturedMinor,
            'tendered_minor' => $capturedMinor,
            'change_minor' => 0,
            'authorized_at' => $capturedAtForPayment,
            'captured_at' => $capturedAtForPayment,
            'metadata' => $metadata,
        ]);

        $this->auditLogger->log(
            $order->merchant_id,
            $order->store_id,
            'payments',
            'payment.captured',
            'payment',
            $payment->id,
            null,
            $this->paymentSnapshot($payment),
            [
                'method' => 'card',
            ],
            null,
            $device->id,
        );
        $this->recordUsage->handle(
            $order->merchant_id,
            $order->store_id,
            'payment.capture.card',
        );

        return $payment->refresh();
    }

    /**
     * @param  array{method: string, amount_minor: int, tip_minor?: int, gift_card_code?: string|null}  $tender
     */
    private function captureGiftCardTender(
        Device $device,
        Order $order,
        array $tender,
        CarbonImmutable $capturedAt,
    ): Payment {
        $giftCardCode = trim((string) ($tender['gift_card_code'] ?? ''));

        if ($giftCardCode === '') {
            throw new DomainException('Gift card tenders require a gift card code.');
        }

        $appliedMinor = (int) $tender['amount_minor'];
        $tipMinor = (int) ($tender['tip_minor'] ?? 0);
        $capturedMinor = $appliedMinor + $tipMinor;
        $hold = $this->storedValueLedger->createHold($device, $giftCardCode, $capturedMinor, $order->id);

        $payment = Payment::query()->create([
            'merchant_id' => $order->merchant_id,
            'store_id' => $order->store_id,
            'order_id' => $order->id,
            'register_session_id' => $order->register_session_id,
            'device_id' => $device->id,
            'method' => 'gift_card',
            'status' => 'captured',
            'amount_minor' => $capturedMinor,
            'applied_minor' => $appliedMinor,
            'tip_minor' => $tipMinor,
            'refundable_minor' => $capturedMinor,
            'tendered_minor' => $capturedMinor,
            'change_minor' => 0,
            'captured_at' => $capturedAt,
            'metadata' => [
                'gift_card_id' => $hold['gift_card_id'],
                'gift_card_code' => $hold['gift_card_code'],
                'hold_id' => $hold['hold_id'],
            ],
        ]);

        $giftCard = $this->storedValueLedger->captureHold($hold['hold_id'], $payment->id);

        $payment->forceFill([
            'metadata' => [
                ...($payment->metadata ?? []),
                'gift_card_balance_minor' => $giftCard['current_balance_minor'] ?? null,
            ],
        ])->save();

        $this->auditLogger->log(
            $order->merchant_id,
            $order->store_id,
            'payments',
            'payment.captured',
            'payment',
            $payment->id,
            null,
            $this->paymentSnapshot($payment),
            [
                'method' => 'gift_card',
            ],
            null,
            $device->id,
        );
        $this->recordUsage->handle(
            $order->merchant_id,
            $order->store_id,
            'payment.capture.gift_card',
        );

        return $payment->refresh();
    }

    /**
     * @param  array<int, Payment>  $payments
     * @return array<string, mixed>
     */
    private function buildReceiptPayload(Order $order, array $payments, CarbonImmutable $capturedAt): array
    {
        return [
            'order_id' => $order->id,
            'order_number' => $order->order_number,
            'customer_id' => $order->customer_id,
            'lines' => $order->lines->map(fn ($line): array => [
                'name' => $line->name,
                'quantity' => $line->quantity,
                'unit_price_minor' => $line->unit_price_minor,
                'discount_minor' => $line->discount_minor,
                'tax_minor' => $line->tax_minor,
                'total_minor' => $line->total_minor,
            ])->values()->all(),
            'subtotal_minor' => $order->subtotal_minor,
            'discount_minor' => $order->discount_minor,
            'tax_minor' => $order->tax_minor,
            'tip_minor' => (int) $order->tip_minor,
            'total_minor' => $order->total_minor,
            'paid_minor' => (int) $order->total_minor + (int) $order->tip_minor,
            'currency' => $order->currency,
            'payments' => array_map(fn (Payment $payment): array => [
                'payment_id' => $payment->id,
                'method' => $payment->method,
                'status' => $payment->status,
                'amount_minor' => $payment->amount_minor,
                'applied_minor' => $payment->applied_minor,
                'tip_minor' => $payment->tip_minor,
                'tendered_minor' => $payment->tendered_minor,
                'change_minor' => $payment->change_minor,
                'provider_key' => $payment->provider_key,
                'provider_transaction_id' => $payment->provider_transaction_id,
                'terminal_reference' => $payment->terminal_reference,
                'masked_pan' => $payment->metadata['masked_pan'] ?? null,
                'auth_code' => $payment->metadata['auth_code'] ?? null,
                'terminal_id' => $payment->metadata['terminal_id'] ?? null,
                'application_label' => $payment->metadata['application_label'] ?? null,
                'aid' => $payment->metadata['aid'] ?? null,
                'tvr' => $payment->metadata['tvr'] ?? null,
                'tsi' => $payment->metadata['tsi'] ?? null,
                'entry_mode' => $payment->metadata['entry_mode'] ?? null,
                'terminal_status_code' => $payment->metadata['terminal_status_code'] ?? null,
                'terminal_result_code' => $payment->metadata['terminal_result_code'] ?? null,
                'terminal_timestamp' => $payment->metadata['terminal_timestamp'] ?? null,
                'transaction_reference' => $payment->metadata['transaction_reference'] ?? $payment->provider_transaction_id,
                'gift_card_code' => $payment->metadata['gift_card_code'] ?? null,
            ], $payments),
            'captured_at' => $capturedAt->toIso8601String(),
        ];
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
            'applied_minor' => $payment->applied_minor,
            'tip_minor' => $payment->tip_minor,
            'refundable_minor' => $payment->refundable_minor,
            'provider_key' => $payment->provider_key,
            'provider_transaction_id' => $payment->provider_transaction_id,
        ];
    }

    private function hasOpenCardInDoubtException(Order $order): bool
    {
        return ExceptionCase::query()
            ->where('merchant_id', $order->merchant_id)
            ->where('store_id', $order->store_id)
            ->where('module', 'payments')
            ->where('code', 'card_in_doubt')
            ->where('status', 'open')
            ->where('related_type', 'order')
            ->where('related_id', $order->id)
            ->exists();
    }

    private function isApprovedTerminalResult(string $terminalStatusCode, string $terminalResultCode): bool
    {
        return in_array($terminalStatusCode, ['approved', 'success', 'ok', '00'], true)
            || in_array($terminalResultCode, ['approved', 'success', 'ok', '00'], true);
    }

    private function isInDoubtTerminalResult(string $terminalStatusCode, string $terminalResultCode): bool
    {
        return in_array($terminalStatusCode, ['timeout', 'no_response', 'in_doubt', 'unknown'], true)
            || in_array($terminalResultCode, ['timeout', 'no_response', 'in_doubt', 'unknown'], true);
    }

    private function parseTerminalTimestamp(?string $timestamp, CarbonImmutable $fallback): CarbonImmutable
    {
        if ($timestamp === null || trim($timestamp) === '') {
            return $fallback;
        }

        try {
            return CarbonImmutable::parse($timestamp)->utc();
        } catch (\Throwable) {
            return $fallback;
        }
    }
}
