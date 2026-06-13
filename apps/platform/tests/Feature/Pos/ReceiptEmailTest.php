<?php

use App\Modules\OrderRegister\Domain\Models\Receipt;
use App\Modules\OrderRegister\Interfaces\Mail\ReceiptMail;
use App\Modules\PlatformCore\Domain\Models\Device;
use Illuminate\Support\Facades\Mail;
use Laravel\Sanctum\Sanctum;

function checkoutReceipt(): array
{
    [$device, $session, $item] = buildPosOrderContext();
    Sanctum::actingAs($device, ['pos:access']);

    $orderId = test()->withHeaders(posHeaders() + ['Idempotency-Key' => 're-1'])
        ->postJson('/api/pos/v1/orders', [
            'register_session_id' => $session->id,
            'lines' => [['catalog_item_id' => $item->id, 'quantity' => 1]],
        ])->json('data.id');

    test()->withHeaders(posHeaders() + ['Idempotency-Key' => 're-co-1'])
        ->postJson("/api/pos/v1/orders/{$orderId}/cash-checkout", ['tendered_minor' => 5000])
        ->assertCreated();

    return [$device, Receipt::query()->firstOrFail()];
}

it('queues a receipt email for a receipt in the device store', function () {
    Mail::fake();
    [$device, $receipt] = checkoutReceipt();

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'email-1'])
        ->postJson("/api/pos/v1/receipts/{$receipt->id}/email", ['email' => 'guest@example.com'])
        ->assertOk()
        ->assertJsonPath('data.status', 'queued');

    Mail::assertQueued(ReceiptMail::class, fn ($mail) => $mail->hasTo('guest@example.com'));
});

it('returns 404 emailing a receipt from another store', function () {
    Mail::fake();
    [$deviceA, $receipt] = checkoutReceipt();

    // A device in a different store.
    [$deviceB] = buildPosOrderContext();
    $other = Device::query()->find($deviceB->id);
    Sanctum::actingAs($other, ['pos:access']);

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'email-x'])
        ->postJson("/api/pos/v1/receipts/{$receipt->id}/email", ['email' => 'x@example.com'])
        ->assertNotFound();

    Mail::assertNothingQueued();
});

it('validates the email address', function () {
    Mail::fake();
    [$device, $receipt] = checkoutReceipt();

    $this->withHeaders(posHeaders() + ['Idempotency-Key' => 'email-bad'])
        ->postJson("/api/pos/v1/receipts/{$receipt->id}/email", ['email' => 'not-an-email'])
        ->assertStatus(422);
});
