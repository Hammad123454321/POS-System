<?php

namespace App\Modules\DeliveryIntegrations\Application\Jobs;

use App\Modules\DeliveryIntegrations\Application\Actions\BuildCanonicalMenuPayload;
use App\Modules\DeliveryIntegrations\Application\DeliveryAdapterRegistry;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Store;
use Carbon\CarbonImmutable;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Throwable;

class PublishDeliveryMenuJob implements ShouldQueue
{
    use Dispatchable;
    use InteractsWithQueue;
    use Queueable;
    use SerializesModels;

    public function __construct(
        public readonly string $deliveryChannelConfigId,
    ) {}

    public function handle(
        DeliveryAdapterRegistry $registry,
        BuildCanonicalMenuPayload $buildCanonicalMenuPayload,
        OpenExceptionCase $openExceptionCase,
    ): void {
        /** @var DeliveryChannelConfig|null $config */
        $config = DeliveryChannelConfig::query()->find($this->deliveryChannelConfigId);
        if ($config === null || ! $config->is_enabled || ! $config->sync_menu_enabled) {
            return;
        }

        /** @var Store $store */
        $store = Store::query()->whereKey($config->store_id)->firstOrFail();
        $menu = $buildCanonicalMenuPayload->handle($store);

        try {
            $adapter = $registry->forChannel((string) $config->channel_key);
            $adapter->publishMenu($config->toArray(), $menu);

            $config->forceFill([
                'last_menu_published_at' => CarbonImmutable::now('UTC'),
            ])->save();
        } catch (Throwable $exception) {
            $openExceptionCase->handle(
                merchantId: $config->merchant_id,
                storeId: $config->store_id,
                module: 'delivery',
                code: 'channel_publish_failed',
                severity: 'high',
                title: 'Delivery channel menu publish failed.',
                details: [
                    'channel_key' => $config->channel_key,
                    'error' => $exception->getMessage(),
                ],
                relatedType: 'delivery_channel_config',
                relatedId: $config->id,
            );

            throw $exception;
        }
    }
}
