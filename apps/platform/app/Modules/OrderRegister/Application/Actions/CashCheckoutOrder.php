<?php

namespace App\Modules\OrderRegister\Application\Actions;

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Receipt;
use App\Modules\Payments\Application\Actions\TenderOrder;
use App\Modules\PlatformCore\Domain\Models\Device;

class CashCheckoutOrder
{
    public function __construct(
        private readonly TenderOrder $tenderOrder,
    ) {}

    public function handle(Device $device, Order $order, int $tenderedMinor): Receipt
    {
        return $this->tenderOrder->handle($device, $order, [[
            'method' => 'cash',
            'amount_minor' => (int) $order->total_minor,
            'tip_minor' => 0,
            'tendered_minor' => $tenderedMinor,
        ]]);
    }
}
