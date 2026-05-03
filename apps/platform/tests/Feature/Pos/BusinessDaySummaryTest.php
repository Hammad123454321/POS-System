<?php

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Platform\Support\Time\BusinessClock;
use Illuminate\Support\Str;
use Laravel\Sanctum\Sanctum;

it('returns the current business day summary for the device store', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();
    $store = $device->store()->firstOrFail();
    $businessDate = app(BusinessClock::class)->businessDateForStore($store);
    $registerSession->forceFill([
        'business_date' => $businessDate,
    ])->save();

    $order = Order::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'register_session_id' => $registerSession->id,
        'device_id' => $device->id,
        'order_number' => 'ORD-'.Str::upper(Str::random(10)),
        'status' => 'paid',
        'business_date' => $businessDate,
        'currency' => 'USD',
        'subtotal_minor' => 1200,
        'tax_minor' => 120,
        'discount_minor' => 0,
        'total_minor' => 1320,
        'paid_minor' => 1320,
        'opened_at' => now(),
        'closed_at' => now(),
    ]);

    Payment::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'order_id' => $order->id,
        'register_session_id' => $registerSession->id,
        'device_id' => $device->id,
        'method' => 'cash',
        'status' => 'captured',
        'amount_minor' => 1320,
        'tendered_minor' => 1500,
        'change_minor' => 180,
        'captured_at' => now(),
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    app(OpenExceptionCase::class)->handle(
        $device->merchant_id,
        $device->store_id,
        'register',
        'register_session.close_variance',
        'medium',
        'Variance review required.',
        ['register_session_id' => $registerSession->id],
        'register_session',
        $registerSession->id,
        $device->id,
    );

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/reports/business-day-summary')
        ->assertOk()
        ->assertJsonPath('paid_orders_count', 1)
        ->assertJsonPath('gross_sales_minor', 1320)
        ->assertJsonPath('cash_sales_minor', 1320)
        ->assertJsonPath('open_register_sessions_count', 1)
        ->assertJsonPath('open_exception_cases_count', 1);
});
