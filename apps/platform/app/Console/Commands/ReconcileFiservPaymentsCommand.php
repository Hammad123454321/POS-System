<?php

namespace App\Console\Commands;

use App\Modules\Billing\Application\RecordUsage;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Application\PaymentProviderRegistry;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;
use Illuminate\Console\Command;

class ReconcileFiservPaymentsCommand extends Command
{
    protected $signature = 'pos:payments:reconcile-fiserv {--hours=24}';

    protected $description = 'Run Fiserv daily-report reconciliation and open exception cases for status mismatches.';

    public function handle(
        PaymentProviderRegistry $paymentProviderRegistry,
        OpenExceptionCase $openExceptionCase,
        RecordUsage $recordUsage,
    ): int {
        $provider = $paymentProviderRegistry->forKey('fiserv_bluepay');
        $windowHours = max(1, (int) $this->option('hours'));
        $windowStart = CarbonImmutable::now('UTC')->subHours($windowHours);

        $payments = Payment::query()
            ->where('method', 'card')
            ->where('provider_key', 'fiserv_bluepay')
            ->whereNotNull('provider_transaction_id')
            ->whereIn('status', ['captured', 'partially_refunded', 'refunded', 'voided', 'in_doubt'])
            ->where('created_at', '>=', $windowStart)
            ->orderBy('created_at')
            ->get();

        $mismatchCount = 0;
        $processed = 0;

        foreach ($payments as $payment) {
            $device = Device::query()->find($payment->device_id);

            if ($device === null) {
                continue;
            }

            $inquiry = $provider->inquire($device, (string) $payment->provider_transaction_id);
            $remoteStatus = (string) ($inquiry['status'] ?? 'unknown');
            $localStatus = $this->normalizeLocalStatus((string) $payment->status);
            $processed++;

            if ($remoteStatus === 'unknown' || $remoteStatus === $localStatus) {
                continue;
            }

            $mismatchCount++;
            $this->ensureReconciliationException($openExceptionCase, $payment, $localStatus, $remoteStatus, $inquiry);
            $recordUsage->handle(
                $payment->merchant_id,
                $payment->store_id,
                'payment.reconciliation.mismatch',
                1,
                [
                    'payment_id' => $payment->id,
                    'provider_transaction_id' => $payment->provider_transaction_id,
                    'local_status' => $localStatus,
                    'remote_status' => $remoteStatus,
                ],
            );
        }

        $this->info("Processed {$processed} payment(s); mismatches: {$mismatchCount}.");

        return self::SUCCESS;
    }

    private function normalizeLocalStatus(string $status): string
    {
        return match ($status) {
            'captured', 'partially_refunded' => 'captured',
            'refunded' => 'refunded',
            'voided' => 'voided',
            'in_doubt' => 'unknown',
            default => 'unknown',
        };
    }

    /**
     * @param  array<string, mixed>  $inquiry
     */
    private function ensureReconciliationException(
        OpenExceptionCase $openExceptionCase,
        Payment $payment,
        string $localStatus,
        string $remoteStatus,
        array $inquiry,
    ): void {
        $existingCase = ExceptionCase::query()
            ->where('merchant_id', $payment->merchant_id)
            ->where('store_id', $payment->store_id)
            ->where('module', 'payments')
            ->where('code', 'reconciliation_mismatch')
            ->where('status', 'open')
            ->where('related_type', 'payment')
            ->where('related_id', $payment->id)
            ->first();

        if ($existingCase !== null) {
            return;
        }

        $openExceptionCase->handle(
            merchantId: $payment->merchant_id,
            storeId: $payment->store_id,
            module: 'payments',
            code: 'reconciliation_mismatch',
            severity: 'high',
            title: 'Reconciliation mismatch detected for card payment.',
            details: [
                'payment_id' => $payment->id,
                'provider_transaction_id' => $payment->provider_transaction_id,
                'local_status' => $localStatus,
                'remote_status' => $remoteStatus,
                'inquiry' => $inquiry,
            ],
            relatedType: 'payment',
            relatedId: $payment->id,
            openedByDeviceId: $payment->device_id,
        );
    }
}
