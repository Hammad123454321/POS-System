<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\DeliveryIntegrations\Application\DeliveryAdapterRegistry;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\OrderRegister\Application\Actions\CreateOrder;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;

class IngestExternalDeliveryOrder
{
    public function __construct(
        private readonly DeliveryAdapterRegistry $deliveryAdapterRegistry,
        private readonly CreateOrder $createOrder,
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    /**
     * @param  array<string, mixed>  $payload
     */
    public function handle(Device $device, string $channelKey, array $payload): ExternalOrderLink
    {
        /** @var DeliveryChannelConfig|null $channelConfig */
        $channelConfig = DeliveryChannelConfig::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('channel_key', $channelKey)
            ->where('is_enabled', true)
            ->first();

        if ($channelConfig === null) {
            throw new DomainException('This delivery channel is not configured for the current store.');
        }

        $externalOrderId = (string) ($payload['external_order_id'] ?? '');
        if ($externalOrderId === '') {
            throw new DomainException('external_order_id is required.');
        }

        $adapter = $this->deliveryAdapterRegistry->forChannel($channelKey);
        $adapterResult = $adapter->receiveOrder($channelConfig->toArray(), $payload);
        $canonicalPayload = is_array($adapterResult['payload'] ?? null)
            ? (array) $adapterResult['payload']
            : $payload;

        /** @var ExternalOrderLink $externalLink */
        $externalLink = DB::transaction(function () use (
            $adapterResult,
            $channelConfig,
            $device,
            $externalOrderId,
            $canonicalPayload
        ): ExternalOrderLink {
            /** @var ExternalOrderLink $externalLink */
            $externalLink = ExternalOrderLink::query()->updateOrCreate(
                [
                    'merchant_id' => $device->merchant_id,
                    'store_id' => $device->store_id,
                    'channel_key' => $channelConfig->channel_key,
                    'external_order_id' => $externalOrderId,
                ],
                [
                    'external_store_id' => (string) ($payload['external_store_id'] ?? ''),
                    'status' => (string) ($adapterResult['status'] ?? 'received'),
                    'payload' => $canonicalPayload,
                    'received_at' => CarbonImmutable::now('UTC'),
                ],
            );

            if ($externalLink->order_id !== null) {
                return $externalLink;
            }

            $registerSession = RegisterSession::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('store_id', $device->store_id)
                ->where('device_id', $device->id)
                ->where('status', 'open')
                ->latest('opened_at')
                ->first();

            if ($registerSession === null) {
                $this->openExceptionCase->handle(
                    merchantId: $device->merchant_id,
                    storeId: $device->store_id,
                    module: 'delivery',
                    code: 'missing_open_register_session',
                    severity: 'medium',
                    title: 'No open register session found for delivery order ingestion.',
                    details: [
                        'channel_key' => $channelConfig->channel_key,
                        'external_order_id' => $externalOrderId,
                    ],
                    relatedType: 'external_order_link',
                    relatedId: $externalLink->id,
                    openedByDeviceId: $device->id,
                );

                return $externalLink;
            }

            $lineInput = $this->resolveLineInput($device, $canonicalPayload);
            if ($lineInput === []) {
                $this->openExceptionCase->handle(
                    merchantId: $device->merchant_id,
                    storeId: $device->store_id,
                    module: 'delivery',
                    code: 'external_order_mapping_failed',
                    severity: 'high',
                    title: 'External delivery order could not be mapped to catalog items.',
                    details: [
                        'channel_key' => $channelConfig->channel_key,
                        'external_order_id' => $externalOrderId,
                        'lines' => $canonicalPayload['lines'] ?? [],
                    ],
                    relatedType: 'external_order_link',
                    relatedId: $externalLink->id,
                    openedByDeviceId: $device->id,
                );

                return $externalLink;
            }

            $order = $this->createOrder->handle($device, $registerSession, $lineInput);
            $externalLink->forceFill([
                'order_id' => $order->id,
                'status' => 'accepted',
                'processed_at' => CarbonImmutable::now('UTC'),
            ])->save();

            return $externalLink->refresh();
        });

        return $externalLink;
    }

    /**
     * @param  array<string, mixed>  $payload
     * @return array<int, array{catalog_item_id: string, quantity: int}>
     */
    private function resolveLineInput(Device $device, array $payload): array
    {
        $lineInput = [];
        $rawLines = is_array($payload['lines'] ?? null) ? $payload['lines'] : [];

        foreach ($rawLines as $line) {
            if (! is_array($line)) {
                continue;
            }

            $quantity = max(1, (int) ($line['quantity'] ?? 1));
            $catalogItemId = $line['catalog_item_id'] ?? null;

            if (is_string($catalogItemId) && $catalogItemId !== '') {
                $exists = CatalogItem::query()
                    ->where('merchant_id', $device->merchant_id)
                    ->whereKey($catalogItemId)
                    ->where('is_active', true)
                    ->exists();

                if ($exists) {
                    $lineInput[] = [
                        'catalog_item_id' => $catalogItemId,
                        'quantity' => $quantity,
                    ];
                }

                continue;
            }

            $sku = (string) ($line['sku'] ?? '');
            if ($sku === '') {
                continue;
            }

            $matchedItem = CatalogItem::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('sku', $sku)
                ->where('is_active', true)
                ->first();

            if ($matchedItem === null) {
                continue;
            }

            $lineInput[] = [
                'catalog_item_id' => $matchedItem->id,
                'quantity' => $quantity,
            ];
        }

        return $lineInput;
    }
}
