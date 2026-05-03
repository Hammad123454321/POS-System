<?php

namespace App\Modules\DeliveryIntegrations\Application\Transformers\UberEats;

use App\Modules\DeliveryIntegrations\Contracts\DeliveryOrderTransformer;

class UberEatsOrderTransformer implements DeliveryOrderTransformer
{
    public function toCanonicalOrder(array $providerPayload): array
    {
        $lineItems = $providerPayload['line_items'] ?? $providerPayload['items'] ?? [];

        return [
            'external_order_id' => $providerPayload['external_order_id'] ?? $providerPayload['order_id'] ?? $providerPayload['id'] ?? null,
            'external_store_id' => $providerPayload['external_store_id'] ?? $providerPayload['store_id'] ?? null,
            'lines' => collect($lineItems)->map(function (array $line): array {
                return [
                    'catalog_item_id' => $line['catalog_item_id'] ?? $line['external_id'] ?? null,
                    'sku' => $line['sku'] ?? $line['merchant_supplied_id'] ?? null,
                    'quantity' => max(1, (int) ($line['quantity'] ?? 1)),
                ];
            })->values()->all(),
            'payload' => $providerPayload,
        ];
    }
}
