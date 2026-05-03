<?php

use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\CustomerValue\Domain\Models\MemberAccount;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\OrderLine;
use App\Modules\Restaurant\Domain\Models\PrinterConfig;
use App\Modules\Restaurant\Domain\Models\PrintRoute;
use Laravel\Sanctum\Sanctum;

it('creates discounted orders with attached customer and member basics', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    $customer = Customer::query()->create([
        'merchant_id' => $device->merchant_id,
        'name' => 'VIP Guest',
        'phone' => '+15550001111',
        'email' => 'vip@example.test',
        'is_active' => true,
    ]);

    $memberAccount = MemberAccount::query()->create([
        'merchant_id' => $device->merchant_id,
        'customer_id' => $customer->id,
        'member_number' => 'MEM-1001',
        'status' => 'active',
    ]);

    $discountRule = DiscountRule::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'name' => 'VIP 10%',
        'code' => 'VIP10',
        'type' => 'percent_basis_points',
        'value_basis_points' => 1000,
        'sort_order' => 1,
        'is_active' => true,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $response = $this->withHeaders(posHeaders(['Idempotency-Key' => 'order-phase-1b']))
        ->postJson('/api/pos/v1/orders', [
            'register_session_id' => $registerSession->id,
            'customer_id' => $customer->id,
            'discount_rule_id' => $discountRule->id,
            'lines' => [
                [
                    'catalog_item_id' => $item->id,
                    'quantity' => 1,
                ],
            ],
        ])
        ->assertCreated()
        ->assertJsonPath('data.customer_id', $customer->id)
        ->assertJsonPath('data.discount_minor', 120)
        ->assertJsonPath('data.tax_minor', 108)
        ->assertJsonPath('data.total_minor', 1188)
        ->json('data');

    $order = Order::query()->findOrFail($response['id']);
    $line = OrderLine::query()->where('order_id', $order->id)->firstOrFail();

    expect($order->member_account_id)->toBe($memberAccount->id);
    expect($order->discount_snapshot['code'])->toBe('VIP10');
    expect($line->discount_minor)->toBe(120);
    expect($line->tax_minor)->toBe(108);
    expect($line->total_minor)->toBe(1188);
});

it('returns customer search results and phase 1b config surfaces for the device store', function () {
    [$device, $registerSession, $item] = buildPosOrderContext();

    Customer::query()->create([
        'merchant_id' => $device->merchant_id,
        'name' => 'VIP Guest',
        'phone' => '+15550001111',
        'email' => 'vip@example.test',
        'is_active' => true,
    ])->memberAccount()->create([
        'merchant_id' => $device->merchant_id,
        'member_number' => 'MEM-1001',
        'status' => 'active',
    ]);

    DiscountRule::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'name' => 'VIP 10%',
        'code' => 'VIP10',
        'type' => 'percent_basis_points',
        'value_basis_points' => 1000,
        'sort_order' => 1,
        'is_active' => true,
    ]);

    $primaryPrinter = PrinterConfig::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'name' => 'Front Receipt Printer',
        'driver_key' => 'network-escpos',
        'connection_config' => ['host' => '192.168.1.50'],
        'is_active' => true,
    ]);

    PrintRoute::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'route_key' => 'receipt-default',
        'document_type' => 'receipt',
        'primary_printer_config_id' => $primaryPrinter->id,
        'is_active' => true,
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/customers/search?q=VIP')
        ->assertOk()
        ->assertJsonPath('data.0.name', 'VIP Guest')
        ->assertJsonPath('data.0.member_account.member_number', 'MEM-1001');

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/config')
        ->assertOk()
        ->assertJsonPath('items.0.id', $item->id)
        ->assertJsonPath('discount_rules.0.code', 'VIP10')
        ->assertJsonPath('print_routes.0.route_key', 'receipt-default')
        ->assertJsonPath('print_routes.0.primary_printer.name', 'Front Receipt Printer');
});
