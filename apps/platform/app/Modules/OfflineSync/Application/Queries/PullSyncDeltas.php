<?php

namespace App\Modules\OfflineSync\Application\Queries;

use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;

class PullSyncDeltas
{
    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device, ?string $cursor): array
    {
        $since = $cursor !== null && $cursor !== '' ? CarbonImmutable::parse($cursor) : null;
        $nextCursor = CarbonImmutable::now('UTC');

        $orders = Order::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->when($since !== null, fn ($query) => $query->where('updated_at', '>', $since))
            ->limit((int) config('pos.sync.delta_page_size', 100))
            ->get(['id', 'status', 'paid_minor', 'updated_at']);

        $registerSessions = RegisterSession::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->when($since !== null, fn ($query) => $query->where('updated_at', '>', $since))
            ->limit((int) config('pos.sync.delta_page_size', 100))
            ->get(['id', 'status', 'expected_cash_minor', 'updated_at']);

        return [
            'cursor' => $nextCursor->toIso8601String(),
            'orders' => $orders->map(fn (Order $order): array => [
                'id' => $order->id,
                'status' => $order->status,
                'paid_minor' => $order->paid_minor,
                'updated_at' => $order->updated_at?->toIso8601String(),
            ])->values()->all(),
            'register_sessions' => $registerSessions->map(fn (RegisterSession $session): array => [
                'id' => $session->id,
                'status' => $session->status,
                'expected_cash_minor' => $session->expected_cash_minor,
                'updated_at' => $session->updated_at?->toIso8601String(),
            ])->values()->all(),
        ];
    }
}
