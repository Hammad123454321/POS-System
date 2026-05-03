<?php

namespace App\Modules\Reporting\Application\Queries;

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\Payment;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Platform\Support\Time\BusinessClock;

class BusinessDaySummaryQuery
{
    public function __construct(
        private readonly BusinessClock $businessClock,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device): array
    {
        $store = $device->store()->firstOrFail();
        $activeSession = RegisterSession::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('status', 'open')
            ->latest('opened_at')
            ->first();
        $businessDate = $activeSession?->business_date?->format('Y-m-d')
            ?? $this->businessClock->businessDateForStore($store);

        return [
            'business_date' => $businessDate,
            'open_orders_count' => Order::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('business_date', $businessDate)
                ->where('status', 'open')
                ->count(),
            'paid_orders_count' => Order::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('business_date', $businessDate)
                ->where('status', 'paid')
                ->count(),
            'gross_sales_minor' => (int) Order::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('business_date', $businessDate)
                ->where('status', 'paid')
                ->sum('total_minor'),
            'cash_sales_minor' => (int) Payment::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('method', 'cash')
                ->where('status', 'captured')
                ->whereHas('order', fn ($query) => $query->where('business_date', $businessDate))
                ->sum('amount_minor'),
            'open_register_sessions_count' => RegisterSession::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('business_date', $businessDate)
                ->where('status', 'open')
                ->count(),
            'open_exception_cases_count' => ExceptionCase::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('status', 'open')
                ->count(),
        ];
    }
}
