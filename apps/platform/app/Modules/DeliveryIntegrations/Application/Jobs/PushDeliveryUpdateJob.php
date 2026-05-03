<?php

namespace App\Modules\DeliveryIntegrations\Application\Jobs;

use App\Modules\DeliveryIntegrations\Application\DeliveryAdapterRegistry;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Throwable;

class PushDeliveryUpdateJob implements ShouldQueue
{
    use Dispatchable;
    use InteractsWithQueue;
    use Queueable;
    use SerializesModels;

    /**
     * @param  array<string, mixed>  $payload
     */
    public function __construct(
        public readonly string $deliveryChannelConfigId,
        public readonly string $operation,
        public readonly array $payload,
    ) {}

    public function handle(
        DeliveryAdapterRegistry $registry,
        OpenExceptionCase $openExceptionCase,
    ): void {
        /** @var DeliveryChannelConfig|null $config */
        $config = DeliveryChannelConfig::query()->find($this->deliveryChannelConfigId);
        if ($config === null || ! $config->is_enabled) {
            return;
        }

        $adapter = $registry->forChannel((string) $config->channel_key);

        try {
            match ($this->operation) {
                'confirm_order' => $adapter->confirmOrder($config->toArray(), $this->payload),
                'update_status' => $adapter->updateStatus($config->toArray(), $this->payload),
                'cancel_order' => $adapter->cancelOrder($config->toArray(), $this->payload),
                'set_item_availability' => $adapter->setItemAvailability($config->toArray(), $this->payload),
                'set_store_availability' => $adapter->setStoreAvailability($config->toArray(), $this->payload),
                default => null,
            };
        } catch (Throwable $exception) {
            $openExceptionCase->handle(
                merchantId: $config->merchant_id,
                storeId: $config->store_id,
                module: 'delivery',
                code: 'channel_operation_failed',
                severity: 'high',
                title: 'Delivery channel operation failed.',
                details: [
                    'channel_key' => $config->channel_key,
                    'operation' => $this->operation,
                    'payload' => $this->payload,
                    'error' => $exception->getMessage(),
                ],
                relatedType: 'delivery_channel_config',
                relatedId: $config->id,
            );

            throw $exception;
        }
    }
}
