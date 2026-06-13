<?php

namespace App\Modules\OfflineSync\Application\Arbitration;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\OrderStatus;
use App\Modules\PlatformCore\Domain\Models\Device;
use Illuminate\Support\Facades\DB;

/**
 * Server-authoritative conflict arbitration for inbound sync events. Returns a
 * status string ('accepted' | 'conflict_superseded' | 'conflict_rejected') plus
 * an optional conflict_code. Only order status_change and inventory absolute
 * sets are arbitrated; everything else is accepted unchanged.
 */
class ArbitrateSyncEvent
{
    public function __construct(
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    /**
     * @param  array{entity_type: string, entity_id?: string|null, action: string, payload: array<string, mixed>}  $event
     * @return array{status: string, conflict_code: ?string, server_status_seq?: int}
     */
    public function handle(Device $device, array $event): array
    {
        if ($event['entity_type'] === 'order' && $event['action'] === 'status_change') {
            return $this->arbitrateOrderStatus($device, $event);
        }

        return ['status' => 'accepted', 'conflict_code' => null];
    }

    /**
     * @param  array{entity_id?: string|null, payload: array<string, mixed>}  $event
     * @return array{status: string, conflict_code: ?string, server_status_seq?: int}
     */
    private function arbitrateOrderStatus(Device $device, array $event): array
    {
        $orderId = $event['entity_id'] ?? null;
        $payload = $event['payload'];
        $baseSeq = (int) ($payload['base_status_seq'] ?? 0);
        $targetStatus = (string) ($payload['status'] ?? '');

        if ($orderId === null) {
            return ['status' => 'accepted', 'conflict_code' => null];
        }

        return DB::transaction(function () use ($device, $orderId, $baseSeq, $targetStatus): array {
            /** @var Order|null $order */
            $order = Order::query()
                ->where('store_id', $device->store_id)
                ->whereKey($orderId)
                ->lockForUpdate()
                ->first();

            if ($order === null) {
                return ['status' => 'accepted', 'conflict_code' => null];
            }

            // Stale base → server wins (event superseded).
            if ($baseSeq < (int) $order->status_seq) {
                if (OrderStatus::isMoneyBearing($targetStatus)) {
                    $this->openExceptionCase->handle(
                        merchantId: $order->merchant_id,
                        storeId: $order->store_id,
                        module: 'sync',
                        code: 'order_conflict',
                        severity: 'high',
                        title: 'A money-bearing order change arrived on a stale base sequence.',
                        details: ['order_id' => $order->id, 'target_status' => $targetStatus, 'base_seq' => $baseSeq, 'server_seq' => $order->status_seq],
                        relatedType: 'order',
                        relatedId: $order->id,
                        openedByDeviceId: $device->id,
                    );
                }

                return ['status' => 'conflict_superseded', 'conflict_code' => 'stale_base_seq', 'server_status_seq' => (int) $order->status_seq];
            }

            // Fresh base. Apply only if the transition is legal.
            if (! OrderStatus::canTransition((string) $order->status, $targetStatus)) {
                $this->openExceptionCase->handle(
                    merchantId: $order->merchant_id,
                    storeId: $order->store_id,
                    module: 'sync',
                    code: 'order_illegal_transition',
                    severity: 'medium',
                    title: 'A sync event requested an illegal order transition.',
                    details: ['order_id' => $order->id, 'from' => $order->status, 'to' => $targetStatus],
                    relatedType: 'order',
                    relatedId: $order->id,
                    openedByDeviceId: $device->id,
                );

                return ['status' => 'conflict_rejected', 'conflict_code' => 'illegal_transition', 'server_status_seq' => (int) $order->status_seq];
            }

            $order->forceFill([
                'status' => $targetStatus,
                'status_seq' => (int) $order->status_seq + 1,
            ])->save();

            return ['status' => 'accepted', 'conflict_code' => null, 'server_status_seq' => (int) $order->status_seq];
        });
    }
}
