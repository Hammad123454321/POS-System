<?php

namespace App\Modules\DeliveryIntegrations\Application\Jobs;

use App\Modules\DeliveryIntegrations\Application\Actions\CancelExternalOrder;
use App\Modules\DeliveryIntegrations\Application\Actions\IngestExternalDeliveryOrder;
use App\Modules\DeliveryIntegrations\Application\Actions\UpdateExternalOrderStatus;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryProviderEvent;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Application\Devices\ResolveSystemDevice;
use App\Modules\PlatformCore\Domain\Models\Store;
use Carbon\CarbonImmutable;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldBeUnique;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;

class ProcessDeliveryWebhookJob implements ShouldBeUnique, ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public int $tries = 5;

    public function __construct(public readonly string $providerEventId)
    {
        $this->onQueue((string) config('pos.delivery.queue', 'delivery'));
    }

    /**
     * @return array<int, int>
     */
    public function backoff(): array
    {
        return [10, 30, 60, 300, 600];
    }

    public function uniqueId(): string
    {
        return $this->providerEventId;
    }

    public function handle(
        ResolveSystemDevice $resolveSystemDevice,
        IngestExternalDeliveryOrder $ingest,
        UpdateExternalOrderStatus $updateStatus,
        CancelExternalOrder $cancel,
        OpenExceptionCase $openExceptionCase,
    ): void {
        /** @var DeliveryProviderEvent|null $event */
        $event = DeliveryProviderEvent::query()->find($this->providerEventId);

        if ($event === null || $event->processed_at !== null || ! $event->signature_valid) {
            return;
        }

        $store = Store::query()->find($event->store_id);
        if ($store === null) {
            $this->markProcessed($event);

            return;
        }

        $payload = $event->payload ?? [];
        $eventType = $event->event_type ?? 'order_create';

        if (in_array($eventType, ['order_create', 'order', 'created'], true)) {
            $device = $resolveSystemDevice->handle($store);
            $link = $ingest->handle($device, $event->channel_key, $payload);
            $event->forceFill(['external_order_link_id' => $link->id])->save();
            $this->markProcessed($event);

            return;
        }

        // Status / cancel events reference an existing link.
        $link = ExternalOrderLink::query()
            ->where('store_id', $store->id)
            ->where('channel_key', $event->channel_key)
            ->where('external_order_id', $event->external_order_id)
            ->first();

        if ($link === null) {
            $openExceptionCase->handle(
                merchantId: $store->merchant_id,
                storeId: $store->id,
                module: 'delivery',
                code: 'unmatched_delivery_event',
                severity: 'medium',
                title: 'Delivery webhook referenced an unknown external order.',
                details: [
                    'channel_key' => $event->channel_key,
                    'external_order_id' => $event->external_order_id,
                    'event_type' => $eventType,
                ],
                relatedType: 'delivery_provider_event',
                relatedId: $event->id,
            );

            $this->markProcessed($event);

            return;
        }

        $device = $resolveSystemDevice->handle($store);

        if (in_array($eventType, ['cancel', 'cancelled', 'order_cancel'], true)) {
            $cancel->handle($device, $link, (string) ($payload['reason'] ?? 'provider_cancelled'));
        } else {
            $updateStatus->handle($device, $link, (string) ($payload['status'] ?? $eventType));
        }

        $event->forceFill(['external_order_link_id' => $link->id])->save();
        $this->markProcessed($event);
    }

    private function markProcessed(DeliveryProviderEvent $event): void
    {
        $event->forceFill(['processed_at' => CarbonImmutable::now('UTC')])->save();
    }
}
