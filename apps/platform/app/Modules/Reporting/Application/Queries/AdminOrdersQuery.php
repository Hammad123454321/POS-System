<?php

namespace App\Modules\Reporting\Application\Queries;

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Reporting\ReportingConnection;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;

class AdminOrdersQuery
{
    public function __construct(
        private readonly ReportingConnection $reporting,
    ) {}

    /**
     * @param  array<string, mixed>  $filters
     */
    public function handle(Store $store, array $filters, int $perPage): LengthAwarePaginator
    {
        $perPage = max(1, min($perPage, 100));

        return $this->reporting->query(Order::class)
            // Belt: explicit tenant scoping in addition to RLS set_config on the
            // reporting connection by ApplyAdminRequestContext.
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->when($filters['status'] ?? null, fn ($q, string $status) => $q->where('status', $status))
            ->when($filters['device_id'] ?? null, fn ($q, string $id) => $q->where('device_id', $id))
            ->when($filters['business_date_from'] ?? null, fn ($q, string $d) => $q->whereDate('business_date', '>=', $d))
            ->when($filters['business_date_to'] ?? null, fn ($q, string $d) => $q->whereDate('business_date', '<=', $d))
            ->when($filters['q'] ?? null, fn ($q, string $term) => $q->where('order_number', 'ilike', "%{$term}%"))
            ->orderByDesc('created_at')
            ->paginate($perPage);
    }

    public function find(Store $store, string $orderId): ?Order
    {
        return $this->reporting->query(Order::class)
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->with(['lines', 'payments.refunds', 'payments.voidRecords', 'paymentSplits', 'receipt'])
            ->find($orderId);
    }
}
