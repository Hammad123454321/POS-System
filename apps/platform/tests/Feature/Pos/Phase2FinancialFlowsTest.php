<?php

use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\OfflineSync\Domain\Models\SyncEvent;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\OrderRegister\Domain\Models\PaymentSplit;
use App\Modules\OrderRegister\Domain\Models\Refund;
use App\Modules\StoredValue\Domain\Models\MembershipPlan;
use Illuminate\Support\Facades\Http;
use Laravel\Sanctum\Sanctum;

it('returns phase 2 payment capabilities and membership plans in config', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    MembershipPlan::query()->create([
        'merchant_id' => $device->merchant_id,
        'name' => 'Monthly VIP',
        'code' => 'VIP-MONTHLY',
        'price_minor' => 2999,
        'currency' => 'USD',
        'duration_days' => 30,
        'benefits_snapshot' => ['discount_basis_points' => 1000],
        'is_active' => true,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/config')
        ->assertOk()
        ->assertJsonPath('items.0.id', $item->id)
        ->assertJsonPath('membership_plans.0.code', 'VIP-MONTHLY')
        ->assertJsonPath('payment_capabilities.default_provider', 'fiserv_bluepay')
        ->assertJsonPath('payment_capabilities.supported_tenders.2', 'gift_card');
});

it('captures split cash and card tenders with tip through the phase 2 tender endpoint', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    Sanctum::actingAs($device, ['pos:access']);

    $orderId = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-order-create',
    ]))->postJson('/api/pos/v1/orders', [
        'register_session_id' => $registerSession->id,
        'lines' => [
            [
                'catalog_item_id' => $item->id,
                'quantity' => 1,
            ],
        ],
    ])->assertCreated()->json('data.id');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-order-tender',
    ]))->postJson("/api/pos/v1/orders/{$orderId}/tenders", [
        'tenders' => [
            [
                'method' => 'cash',
                'amount_minor' => 500,
                'tendered_minor' => 500,
            ],
            [
                'method' => 'card',
                'amount_minor' => 820,
                'tip_minor' => 200,
                'terminal_reference' => 'PAX-TERM-001',
                'provider_key' => 'fiserv_bluepay',
                'provider_transaction_id' => '100000000001',
                'auth_code' => 'ALVSGO',
                'masked_pan' => '************1111',
                'terminal_id' => 'PAX-A920-01',
                'entry_mode' => 'chip',
                'application_label' => 'VISA CREDIT',
                'aid' => 'A0000000031010',
                'tvr' => '0000008000',
                'tsi' => 'E800',
                'terminal_status_code' => 'approved',
                'terminal_result_code' => '00',
                'terminal_timestamp' => now()->toIso8601String(),
            ],
        ],
    ])->assertCreated()
        ->assertJsonPath('data.payload.payments.0.method', 'cash')
        ->assertJsonPath('data.payload.payments.1.method', 'card')
        ->assertJsonPath('data.payload.payments.1.tip_minor', 200);

    $order = Order::query()->findOrFail($orderId);
    $cardPayment = Payment::query()
        ->where('order_id', $orderId)
        ->where('method', 'card')
        ->firstOrFail();

    expect($order->status)->toBe('paid');
    expect($order->tip_minor)->toBe(200);
    expect($order->paid_minor)->toBe(1520);
    expect($registerSession->refresh()->expected_cash_minor)->toBe(10500);
    expect($cardPayment->status)->toBe('captured');
    expect($cardPayment->amount_minor)->toBe(1020);
    expect($cardPayment->metadata['aid'])->toBe('A0000000031010');
    expect(PaymentSplit::query()->where('order_id', $orderId)->count())->toBe(2);
});

it('issues tops up redeems and refunds gift card tenders with online balance lookup', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    Sanctum::actingAs($device, ['pos:access']);

    $issued = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-gift-card-issue',
    ]))->postJson('/api/pos/v1/gift-cards/issue', [
        'amount_minor' => 3000,
    ])->assertCreated()
        ->assertJsonPath('data.label_payload.document_type', 'label')
        ->json('data');

    $giftCardCode = $issued['code'];

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/gift-cards/lookup?code='.$giftCardCode)
        ->assertOk()
        ->assertJsonPath('data.current_balance_minor', 3000);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-gift-card-top-up',
    ]))->postJson('/api/pos/v1/gift-cards/top-up', [
        'gift_card_code' => $giftCardCode,
        'amount_minor' => 500,
    ])->assertCreated()
        ->assertJsonPath('data.current_balance_minor', 3500);

    $orderId = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-gift-card-order-create',
    ]))->postJson('/api/pos/v1/orders', [
        'register_session_id' => $registerSession->id,
        'lines' => [
            [
                'catalog_item_id' => $item->id,
                'quantity' => 1,
            ],
        ],
    ])->assertCreated()->json('data.id');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-gift-card-tender',
    ]))->postJson("/api/pos/v1/orders/{$orderId}/tenders", [
        'tenders' => [
            [
                'method' => 'gift_card',
                'amount_minor' => 1320,
                'gift_card_code' => $giftCardCode,
            ],
        ],
    ])->assertCreated()
        ->assertJsonPath('data.payload.payments.0.method', 'gift_card');

    $giftCardPayment = Payment::query()
        ->where('order_id', $orderId)
        ->where('method', 'gift_card')
        ->firstOrFail();

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/gift-cards/lookup?code='.$giftCardCode)
        ->assertOk()
        ->assertJsonPath('data.current_balance_minor', 2180);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-gift-card-refund',
    ]))->postJson("/api/pos/v1/payments/{$giftCardPayment->id}/refunds", [
        'amount_minor' => 1320,
        'reason' => 'guest changed mind',
    ])->assertCreated()
        ->assertJsonPath('data.status', 'refunded');

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/gift-cards/lookup?code='.$giftCardCode)
        ->assertOk()
        ->assertJsonPath('data.current_balance_minor', 3500);
});

it('activates and looks up memberships with online merchant-wide validation', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    $customer = Customer::query()->create([
        'merchant_id' => $device->merchant_id,
        'name' => 'VIP Guest',
        'phone' => '+15550001111',
        'email' => 'vip@example.test',
        'is_active' => true,
    ]);

    $plan = MembershipPlan::query()->create([
        'merchant_id' => $device->merchant_id,
        'name' => 'Monthly VIP',
        'code' => 'VIP-MONTHLY',
        'price_minor' => 2999,
        'currency' => 'USD',
        'duration_days' => 30,
        'benefits_snapshot' => ['discount_basis_points' => 1000],
        'is_active' => true,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $activation = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-membership-activate',
    ]))->postJson('/api/pos/v1/memberships/activate', [
        'customer_id' => $customer->id,
        'membership_plan_id' => $plan->id,
        'member_number' => 'VIP-1001',
    ])->assertCreated()
        ->assertJsonPath('data.customer.id', $customer->id)
        ->assertJsonPath('data.membership_plan.id', $plan->id)
        ->json('data');

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/memberships/lookup?member_number=VIP-1001')
        ->assertOk()
        ->assertJsonPath('data.member_account_id', $activation['member_account_id'])
        ->assertJsonPath('data.membership_plan.code', 'VIP-MONTHLY');

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/memberships/lookup?customer_id='.$customer->id)
        ->assertOk()
        ->assertJsonPath('data.member_number', 'VIP-1001');
});

it('starts sync recovery runs and marks recoverable events as recovered', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    Sanctum::actingAs($device, ['pos:access']);

    $syncEvent = SyncEvent::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'device_id' => $device->id,
        'local_event_id' => 'sync-recovery-1',
        'entity_type' => 'register_session',
        'entity_id' => $registerSession->id,
        'action' => 'opened',
        'payload' => [
            'register_session_id' => $registerSession->id,
        ],
        'status' => 'error',
        'error_code' => 'SIMULATED_FAILURE',
        'received_at' => now(),
    ]);

    $run = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-sync-recovery-run',
    ]))->postJson('/api/pos/v1/sync/recovery-runs')
        ->assertAccepted()
        ->assertJsonPath('data.event_count', 1)
        ->json('data');

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/sync/recovery-runs/'.$run['id'])
        ->assertOk()
        ->assertJsonPath('data.event_count', 1);

    expect($syncEvent->fresh()->status)->toBe('recovered');
    expect($syncEvent->fresh()->recovery_attempts)->toBe(1);
});

it('refunds card payments through the phase 2 refund endpoint', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    config()->set('pos.payments.fiserv_bluepay', [
        'account_id' => '123412341234',
        'secret_key' => 'abcdabcdabcdabcd',
        'hash_type' => 'HMAC_SHA256',
        'mode' => 'TEST',
        'bp20post_url' => 'https://secure.bluepay.com/interfaces/bp20post',
        'daily_report_url' => 'https://secure.bluepay.com/interfaces/bpdailyreport2',
        'webhook' => ['verify_bp_stamp' => true],
    ]);
    Http::fake([
        'https://secure.bluepay.com/interfaces/bp20post' => Http::response(
            'TRANS_ID=100000999999&STATUS=1&MESSAGE=Approved+Refund&ACCOUNT_ID=123412341234',
            200,
            ['Content-Type' => 'text/plain'],
        ),
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $orderId = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-card-refund-order-create',
    ]))->postJson('/api/pos/v1/orders', [
        'register_session_id' => $registerSession->id,
        'lines' => [
            [
                'catalog_item_id' => $item->id,
                'quantity' => 1,
            ],
        ],
    ])->assertCreated()->json('data.id');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-card-refund-order-tender',
    ]))->postJson("/api/pos/v1/orders/{$orderId}/tenders", [
        'tenders' => [
            [
                'method' => 'card',
                'amount_minor' => 1320,
                'tip_minor' => 180,
                'terminal_reference' => 'PAX-TERM-002',
                'provider_key' => 'fiserv_bluepay',
                'provider_transaction_id' => '100000000002',
                'auth_code' => 'ZJUDQD',
                'masked_pan' => '************2222',
                'terminal_id' => 'PAX-A920-02',
                'entry_mode' => 'contactless',
                'application_label' => 'VISA CREDIT',
                'aid' => 'A0000000031010',
                'tvr' => '0000008000',
                'tsi' => 'E800',
                'terminal_status_code' => 'approved',
                'terminal_result_code' => '00',
                'terminal_timestamp' => now()->toIso8601String(),
            ],
        ],
    ])->assertCreated();

    $payment = Payment::query()
        ->where('order_id', $orderId)
        ->where('method', 'card')
        ->firstOrFail();

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-2-card-refund',
    ]))->postJson("/api/pos/v1/payments/{$payment->id}/refunds", [
        'amount_minor' => 500,
        'reason' => 'partial refund',
    ])->assertCreated()
        ->assertJsonPath('data.amount_minor', 500)
        ->assertJsonPath('data.status', 'partially_refunded');

    expect($payment->fresh()->status)->toBe('partially_refunded');
    expect($payment->fresh()->refundable_minor)->toBe(1000);
    expect(Refund::query()->where('payment_id', $payment->id)->count())->toBe(1);
});
