<?php

namespace App\Modules\Reporting\Application\Queries;

use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Reporting\ReportingConnection;
use Carbon\CarbonImmutable;

class DeliveryOperationalHealthQuery
{
    public function __construct(
        private readonly ReportingConnection $reportingConnection,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(Store $store, ?string $from = null, ?string $to = null): array
    {
        $fromAt = $from ? CarbonImmutable::parse($from, 'UTC') : CarbonImmutable::now('UTC')->subDay();
        $toAt = $to ? CarbonImmutable::parse($to, 'UTC') : CarbonImmutable::now('UTC');

        $base = $this->reportingConnection->query(ExternalOrderLink::class)
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereBetween('received_at', [$fromAt, $toAt]);

        return [
            'from' => $fromAt->toIso8601String(),
            'to' => $toAt->toIso8601String(),
            'received_orders_count' => (clone $base)->count(),
            'accepted_orders_count' => (clone $base)->where('status', 'accepted')->count(),
            'completed_orders_count' => (clone $base)->where('status', 'completed')->count(),
            'cancelled_orders_count' => (clone $base)->where('status', 'cancelled')->count(),
            'open_delivery_exceptions_count' => $this->reportingConnection->query(ExceptionCase::class)
                ->where('merchant_id', $store->merchant_id)
                ->where('store_id', $store->id)
                ->where('module', 'delivery')
                ->where('status', 'open')
                ->count(),
        ];
    }
}
