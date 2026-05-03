<?php

namespace App\Modules\StoredValue\Application;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\StoredValue\Contracts\StoredValueLedger;
use App\Modules\StoredValue\Domain\Models\GiftCard;
use App\Modules\StoredValue\Domain\Models\GiftCardLedgerEntry;
use App\Modules\StoredValue\Domain\Models\SpendHold;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class EloquentStoredValueLedger implements StoredValueLedger
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
        private readonly RecordUsage $recordUsage,
    ) {}

    public function issue(Store $store, ?string $customerId, int $amountMinor, ?string $requestedCode = null): array
    {
        if ($amountMinor <= 0) {
            throw new DomainException('Gift card issue amount must be greater than zero.');
        }

        return DB::transaction(function () use ($amountMinor, $customerId, $requestedCode, $store): array {
            $code = $requestedCode === null || $requestedCode === ''
                ? $this->generateGiftCardCode()
                : Str::upper($requestedCode);

            $giftCard = GiftCard::query()->create([
                'merchant_id' => $store->merchant_id,
                'issued_to_customer_id' => $customerId,
                'code' => $code,
                'currency' => $store->merchant()->firstOrFail()->currency,
                'status' => 'active',
                'current_balance_minor' => $amountMinor,
                'activated_at' => CarbonImmutable::now('UTC'),
            ]);

            GiftCardLedgerEntry::query()->create([
                'merchant_id' => $store->merchant_id,
                'store_id' => $store->id,
                'gift_card_id' => $giftCard->id,
                'entry_type' => 'issue',
                'amount_minor' => $amountMinor,
                'balance_after_minor' => $amountMinor,
                'occurred_at' => CarbonImmutable::now('UTC'),
            ]);

            $this->auditLogger->log(
                $store->merchant_id,
                $store->id,
                'stored_value',
                'gift_card.issued',
                'gift_card',
                $giftCard->id,
                null,
                [
                    'code' => $giftCard->code,
                    'current_balance_minor' => $giftCard->current_balance_minor,
                ],
            );
            $this->recordUsage->handle(
                $store->merchant_id,
                $store->id,
                'gift_card.issue',
            );

            return $this->giftCardPayload($giftCard);
        });
    }

    public function topUp(Device $device, string $giftCardCode, int $amountMinor): array
    {
        if ($amountMinor <= 0) {
            throw new DomainException('Gift card top-up amount must be greater than zero.');
        }

        return DB::transaction(function () use ($amountMinor, $device, $giftCardCode): array {
            $giftCard = $this->resolveGiftCard($device->merchant_id, $giftCardCode);
            $beforeState = $this->giftCardPayload($giftCard);
            $newBalance = $giftCard->current_balance_minor + $amountMinor;

            $giftCard->forceFill([
                'current_balance_minor' => $newBalance,
                'last_used_at' => CarbonImmutable::now('UTC'),
            ])->save();

            GiftCardLedgerEntry::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'gift_card_id' => $giftCard->id,
                'device_id' => $device->id,
                'entry_type' => 'top_up',
                'amount_minor' => $amountMinor,
                'balance_after_minor' => $newBalance,
                'occurred_at' => CarbonImmutable::now('UTC'),
            ]);

            $this->auditLogger->log(
                $device->merchant_id,
                $device->store_id,
                'stored_value',
                'gift_card.topped_up',
                'gift_card',
                $giftCard->id,
                $beforeState,
                $this->giftCardPayload($giftCard),
                null,
                null,
                $device->id,
            );
            $this->recordUsage->handle(
                $device->merchant_id,
                $device->store_id,
                'gift_card.top_up',
            );

            return $this->giftCardPayload($giftCard);
        });
    }

    public function balance(string $merchantId, string $giftCardCode): array
    {
        return $this->giftCardPayload($this->resolveGiftCard($merchantId, $giftCardCode));
    }

    public function createHold(Device $device, string $giftCardCode, int $amountMinor, ?string $orderId = null): array
    {
        if ($amountMinor <= 0) {
            throw new DomainException('Gift card hold amount must be greater than zero.');
        }

        return DB::transaction(function () use ($amountMinor, $device, $giftCardCode, $orderId): array {
            $giftCard = $this->resolveGiftCard($device->merchant_id, $giftCardCode);
            $activeHeldMinor = (int) SpendHold::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('resource_type', 'gift_card')
                ->where('resource_id', $giftCard->id)
                ->where('status', 'active')
                ->where('expires_at', '>', CarbonImmutable::now('UTC'))
                ->sum('amount_minor');

            if (($giftCard->current_balance_minor - $activeHeldMinor) < $amountMinor) {
                throw new DomainException('Gift card balance is insufficient for the requested hold.');
            }

            $hold = SpendHold::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'device_id' => $device->id,
                'order_id' => $orderId,
                'resource_type' => 'gift_card',
                'resource_id' => $giftCard->id,
                'amount_minor' => $amountMinor,
                'status' => 'active',
                'heartbeat_at' => CarbonImmutable::now('UTC'),
                'expires_at' => CarbonImmutable::now('UTC')->addSeconds((int) config('pos.stored_value.hold_ttl_seconds', 120)),
                'metadata' => [
                    'gift_card_code' => $giftCard->code,
                ],
            ]);

            return [
                'hold_id' => $hold->id,
                'gift_card_id' => $giftCard->id,
                'gift_card_code' => $giftCard->code,
                'amount_minor' => $hold->amount_minor,
                'expires_at' => $hold->expires_at?->toIso8601String(),
            ];
        });
    }

    public function captureHold(string $holdId, ?string $paymentId = null): array
    {
        return DB::transaction(function () use ($holdId, $paymentId): array {
            $hold = SpendHold::query()->findOrFail($holdId);

            if ($hold->status !== 'active' || $hold->expires_at?->isPast()) {
                throw new DomainException('Only active gift card holds may be captured.');
            }

            $giftCard = GiftCard::query()->findOrFail($hold->resource_id);
            $beforeState = $this->giftCardPayload($giftCard);
            $newBalance = $giftCard->current_balance_minor - $hold->amount_minor;

            if ($newBalance < 0) {
                throw new DomainException('Gift card balance cannot fall below zero.');
            }

            $giftCard->forceFill([
                'current_balance_minor' => $newBalance,
                'last_used_at' => CarbonImmutable::now('UTC'),
            ])->save();

            GiftCardLedgerEntry::query()->create([
                'merchant_id' => $hold->merchant_id,
                'store_id' => $hold->store_id,
                'gift_card_id' => $giftCard->id,
                'order_id' => $hold->order_id,
                'payment_id' => $paymentId,
                'device_id' => $hold->device_id,
                'entry_type' => 'redeem',
                'amount_minor' => -1 * $hold->amount_minor,
                'balance_after_minor' => $newBalance,
                'metadata' => $hold->metadata,
                'occurred_at' => CarbonImmutable::now('UTC'),
            ]);

            $hold->forceFill([
                'status' => 'captured',
            ])->save();

            $this->auditLogger->log(
                $hold->merchant_id,
                $hold->store_id,
                'stored_value',
                'gift_card.redeemed',
                'gift_card',
                $giftCard->id,
                $beforeState,
                $this->giftCardPayload($giftCard),
                [
                    'hold_id' => $hold->id,
                    'payment_id' => $paymentId,
                ],
                null,
                $hold->device_id,
            );
            $this->recordUsage->handle(
                $hold->merchant_id,
                $hold->store_id,
                'gift_card.redeem',
            );

            return $this->giftCardPayload($giftCard);
        });
    }

    public function releaseHold(string $holdId): array
    {
        $hold = SpendHold::query()->findOrFail($holdId);

        $hold->forceFill(['status' => 'released'])->save();

        return [
            'hold_id' => $hold->id,
            'status' => $hold->status,
        ];
    }

    public function credit(
        Device $device,
        string $giftCardCode,
        int $amountMinor,
        ?string $paymentId = null,
        ?string $orderId = null,
        ?string $reason = null,
    ): array {
        if ($amountMinor <= 0) {
            throw new DomainException('Gift card credit amount must be greater than zero.');
        }

        return DB::transaction(function () use ($amountMinor, $device, $giftCardCode, $orderId, $paymentId, $reason): array {
            $giftCard = $this->resolveGiftCard($device->merchant_id, $giftCardCode);
            $beforeState = $this->giftCardPayload($giftCard);
            $newBalance = $giftCard->current_balance_minor + $amountMinor;

            $giftCard->forceFill([
                'current_balance_minor' => $newBalance,
                'last_used_at' => CarbonImmutable::now('UTC'),
            ])->save();

            GiftCardLedgerEntry::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'gift_card_id' => $giftCard->id,
                'order_id' => $orderId,
                'payment_id' => $paymentId,
                'device_id' => $device->id,
                'entry_type' => 'refund',
                'amount_minor' => $amountMinor,
                'balance_after_minor' => $newBalance,
                'metadata' => [
                    'reason' => $reason,
                ],
                'occurred_at' => CarbonImmutable::now('UTC'),
            ]);

            $this->auditLogger->log(
                $device->merchant_id,
                $device->store_id,
                'stored_value',
                'gift_card.refunded',
                'gift_card',
                $giftCard->id,
                $beforeState,
                $this->giftCardPayload($giftCard),
                [
                    'payment_id' => $paymentId,
                    'order_id' => $orderId,
                    'reason' => $reason,
                ],
                null,
                $device->id,
            );
            $this->recordUsage->handle(
                $device->merchant_id,
                $device->store_id,
                'gift_card.refund',
            );

            return $this->giftCardPayload($giftCard);
        });
    }

    private function resolveGiftCard(string $merchantId, string $giftCardCode): GiftCard
    {
        /** @var GiftCard $giftCard */
        $giftCard = GiftCard::query()
            ->where('merchant_id', $merchantId)
            ->where('code', Str::upper($giftCardCode))
            ->where('status', 'active')
            ->firstOrFail();

        return $giftCard;
    }

    /**
     * @return array<string, mixed>
     */
    private function giftCardPayload(GiftCard $giftCard): array
    {
        return [
            'id' => $giftCard->id,
            'code' => $giftCard->code,
            'currency' => $giftCard->currency,
            'status' => $giftCard->status,
            'current_balance_minor' => $giftCard->current_balance_minor,
            'issued_to_customer_id' => $giftCard->issued_to_customer_id,
        ];
    }

    private function generateGiftCardCode(): string
    {
        return Str::upper(config('pos.stored_value.gift_card_code_prefix', 'GC').Str::random(10));
    }
}
