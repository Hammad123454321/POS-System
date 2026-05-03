<?php

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\Payments\Domain\Models\PaymentProviderEvent;
use Illuminate\Support\Facades\Http;
use Laravel\Sanctum\Sanctum;

it('opens card in-doubt exceptions and blocks immediate retry for the same order', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

    $orderId = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'fiserv-indoubt-order-create',
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
        'Idempotency-Key' => 'fiserv-indoubt-order-tender-1',
    ]))->postJson("/api/pos/v1/orders/{$orderId}/tenders", [
        'tenders' => [
            [
                'method' => 'card',
                'amount_minor' => 1320,
                'provider_key' => 'fiserv_bluepay',
                'provider_transaction_id' => '100001000001',
                'auth_code' => 'TIMEOUT',
                'masked_pan' => '************1111',
                'terminal_id' => 'PAX-A920-01',
                'entry_mode' => 'chip',
                'aid' => 'A0000000031010',
                'tvr' => '0000008000',
                'tsi' => 'E800',
                'terminal_status_code' => 'timeout',
                'terminal_result_code' => 'no_response',
                'terminal_timestamp' => now()->toIso8601String(),
            ],
        ],
    ])->assertStatus(409)
        ->assertJsonPath('error_code', 'CARD_IN_DOUBT');

    expect(ExceptionCase::query()
        ->where('module', 'payments')
        ->where('code', 'card_in_doubt')
        ->where('related_type', 'order')
        ->where('related_id', $orderId)
        ->count())->toBe(1);

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'fiserv-indoubt-order-tender-2',
    ]))->postJson("/api/pos/v1/orders/{$orderId}/tenders", [
        'tenders' => [
            [
                'method' => 'card',
                'amount_minor' => 1320,
                'provider_key' => 'fiserv_bluepay',
                'provider_transaction_id' => '100001000002',
                'auth_code' => 'ALVSGO',
                'masked_pan' => '************1111',
                'terminal_id' => 'PAX-A920-01',
                'entry_mode' => 'chip',
                'aid' => 'A0000000031010',
                'tvr' => '0000008000',
                'tsi' => 'E800',
                'terminal_status_code' => 'approved',
                'terminal_result_code' => '00',
                'terminal_timestamp' => now()->toIso8601String(),
            ],
        ],
    ])->assertStatus(422)
        ->assertJsonPath('message', 'This order has an unresolved in-doubt card transaction. Complete inquiry/reconciliation before retrying.');

    expect(Order::query()->findOrFail($orderId)->status)->toBe('open');
});

it('voids captured fiserv card payments and maps provider success response', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

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
            'TRANS_ID=100000888888&STATUS=1&MESSAGE=Approved+Void&ACCOUNT_ID=123412341234',
            200,
            ['Content-Type' => 'text/plain'],
        ),
    ]);

    $orderId = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'fiserv-void-order-create',
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
        'Idempotency-Key' => 'fiserv-void-order-tender',
    ]))->postJson("/api/pos/v1/orders/{$orderId}/tenders", [
        'tenders' => [
            [
                'method' => 'card',
                'amount_minor' => 1320,
                'provider_key' => 'fiserv_bluepay',
                'provider_transaction_id' => '100001200001',
                'auth_code' => 'ALVSGO',
                'masked_pan' => '************1111',
                'terminal_id' => 'PAX-A920-03',
                'entry_mode' => 'chip',
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
        'Idempotency-Key' => 'fiserv-void-payment',
    ]))->postJson("/api/pos/v1/payments/{$payment->id}/void", [
        'reason' => 'same-day reversal',
    ])->assertCreated()
        ->assertJsonPath('data.status', 'voided');

    expect($payment->refresh()->status)->toBe('voided');
});

it('verifies fiserv trans-notify signatures and deduplicates webhook events', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

    config()->set('pos.payments.fiserv_bluepay', [
        'account_id' => '123412341234',
        'secret_key' => 'abcdabcdabcdabcd',
        'hash_type' => 'HMAC_SHA256',
        'mode' => 'TEST',
        'bp20post_url' => 'https://secure.bluepay.com/interfaces/bp20post',
        'daily_report_url' => 'https://secure.bluepay.com/interfaces/bpdailyreport2',
        'webhook' => ['verify_bp_stamp' => true],
    ]);

    $orderId = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'fiserv-webhook-order-create',
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
        'Idempotency-Key' => 'fiserv-webhook-order-tender',
    ]))->postJson("/api/pos/v1/orders/{$orderId}/tenders", [
        'tenders' => [
            [
                'method' => 'card',
                'amount_minor' => 1320,
                'provider_key' => 'fiserv_bluepay',
                'provider_transaction_id' => '100001900001',
                'auth_code' => 'ALVSGO',
                'masked_pan' => '************1111',
                'terminal_id' => 'PAX-A920-04',
                'entry_mode' => 'chip',
                'aid' => 'A0000000031010',
                'tvr' => '0000008000',
                'tsi' => 'E800',
                'terminal_status_code' => 'approved',
                'terminal_result_code' => '00',
                'terminal_timestamp' => now()->toIso8601String(),
            ],
        ],
    ])->assertCreated();

    $payload = [
        'account_id' => '123412341234',
        'tps_hash_type' => 'HMAC_SHA256',
        'bp_stamp_def' => 'trans_id trans_status trans_type amount batch_id batch_status total_count total_amount bupload_id rebill_id reb_amount status',
        'trans_id' => '100001900001',
        'trans_status' => '1',
        'trans_type' => 'SALE',
        'amount' => '13.20',
        'batch_id' => '',
        'batch_status' => '',
        'total_count' => '',
        'total_amount' => '',
        'bupload_id' => '',
        'rebill_id' => '',
        'reb_amount' => '',
        'status' => '',
        'issue_date' => '2026-04-24 12:00:00',
    ];
    $message = $payload['trans_id'].$payload['trans_status'].$payload['trans_type'].$payload['amount'].
        $payload['batch_id'].$payload['batch_status'].$payload['total_count'].$payload['total_amount'].
        $payload['bupload_id'].$payload['rebill_id'].$payload['reb_amount'].$payload['status'];
    $payload['bp_stamp'] = hash_hmac('sha256', $message, 'abcdabcdabcdabcd');

    $this->post('/api/webhooks/fiserv/trans-notify', $payload)->assertOk();
    $this->post('/api/webhooks/fiserv/trans-notify', $payload)->assertOk();

    expect(PaymentProviderEvent::query()
        ->where('provider_key', 'fiserv_bluepay')
        ->where('provider_transaction_id', '100001900001')
        ->where('event_type', 'trans_notify.sale')
        ->count())->toBe(1);

    $invalidPayload = [...$payload, 'bp_stamp' => 'invalid'];
    $this->post('/api/webhooks/fiserv/trans-notify', $invalidPayload)
        ->assertStatus(422)
        ->assertJsonPath('error_code', 'WEBHOOK_SIGNATURE_INVALID');

    expect(ExceptionCase::query()
        ->where('module', 'payments')
        ->where('code', 'webhook_signature_failure')
        ->where('status', 'open')
        ->count())->toBe(1);
});
