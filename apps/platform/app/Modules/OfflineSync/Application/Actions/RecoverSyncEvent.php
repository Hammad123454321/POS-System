<?php

namespace App\Modules\OfflineSync\Application\Actions;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\OfflineSync\Domain\Models\SyncEvent;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\Restaurant\Domain\Models\TableAssignment;
use Carbon\CarbonImmutable;

class RecoverSyncEvent
{
    public function __construct(
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    public function handle(string $syncEventId): void
    {
        /** @var SyncEvent $syncEvent */
        $syncEvent = SyncEvent::query()->findOrFail($syncEventId);
        $attempts = (int) $syncEvent->recovery_attempts + 1;
        $recovered = $this->isRecovered($syncEvent);

        if ($recovered) {
            $syncEvent->forceFill([
                'status' => 'recovered',
                'error_code' => null,
                'recovery_attempts' => $attempts,
                'processed_at' => CarbonImmutable::now('UTC'),
            ])->save();

            return;
        }

        $nextStatus = $attempts >= 3 ? 'manual_review' : 'error';

        $syncEvent->forceFill([
            'status' => $nextStatus,
            'error_code' => 'RECOVERY_NOT_VERIFIED',
            'recovery_attempts' => $attempts,
            'processed_at' => null,
        ])->save();

        if ($nextStatus === 'manual_review') {
            $this->openExceptionCase->handle(
                $syncEvent->merchant_id,
                $syncEvent->store_id,
                'sync',
                'sync_event.recovery_failed',
                'medium',
                'A sync event could not be automatically recovered and requires manual review.',
                [
                    'sync_event_id' => $syncEvent->id,
                    'entity_type' => $syncEvent->entity_type,
                    'action' => $syncEvent->action,
                    'recovery_attempts' => $attempts,
                ],
                'sync_event',
                $syncEvent->id,
                $syncEvent->device_id,
            );
        }
    }

    private function isRecovered(SyncEvent $syncEvent): bool
    {
        $entityId = $syncEvent->entity_id ?? $syncEvent->payload['id'] ?? $syncEvent->payload['order_id'] ?? $syncEvent->payload['register_session_id'] ?? $syncEvent->payload['dining_table_id'] ?? null;

        return match ($syncEvent->entity_type) {
            'order' => $this->orderRecovered($syncEvent, $entityId),
            'register_session' => $this->registerSessionRecovered($syncEvent, $entityId),
            'dining_table' => $this->diningTableRecovered($syncEvent, $entityId),
            default => false,
        };
    }

    private function orderRecovered(SyncEvent $syncEvent, mixed $entityId): bool
    {
        if (! is_string($entityId) || $entityId === '') {
            return false;
        }

        /** @var Order|null $order */
        $order = Order::query()
            ->where('merchant_id', $syncEvent->merchant_id)
            ->where('store_id', $syncEvent->store_id)
            ->find($entityId);

        if ($order === null) {
            return false;
        }

        return match ($syncEvent->action) {
            'created' => in_array($order->status, ['open', 'paid'], true),
            'cash_checked_out', 'tendered' => $order->status === 'paid'
                && $order->paid_minor >= (int) ($syncEvent->payload['total_minor'] ?? 0),
            default => false,
        };
    }

    private function registerSessionRecovered(SyncEvent $syncEvent, mixed $entityId): bool
    {
        if (! is_string($entityId) || $entityId === '') {
            return false;
        }

        /** @var RegisterSession|null $registerSession */
        $registerSession = RegisterSession::query()
            ->where('merchant_id', $syncEvent->merchant_id)
            ->where('store_id', $syncEvent->store_id)
            ->find($entityId);

        if ($registerSession === null) {
            return false;
        }

        return match ($syncEvent->action) {
            'opened' => in_array($registerSession->status, ['open', 'closed'], true),
            'closed' => $registerSession->status === 'closed',
            default => false,
        };
    }

    private function diningTableRecovered(SyncEvent $syncEvent, mixed $entityId): bool
    {
        if (! is_string($entityId) || $entityId === '') {
            return false;
        }

        /** @var TableAssignment|null $assignment */
        $assignment = TableAssignment::query()
            ->where('merchant_id', $syncEvent->merchant_id)
            ->where('dining_table_id', $entityId)
            ->first();

        if ($assignment === null) {
            return false;
        }

        return match ($syncEvent->action) {
            'claimed' => $assignment->status === 'occupied' && $assignment->assigned_device_id === $syncEvent->device_id,
            'released' => $assignment->status === 'available',
            default => false,
        };
    }
}
