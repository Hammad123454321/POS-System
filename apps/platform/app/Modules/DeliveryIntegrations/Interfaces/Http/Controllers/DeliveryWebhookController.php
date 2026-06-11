<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\DeliveryIntegrations\Application\DeliveryAdapterRegistry;
use App\Modules\DeliveryIntegrations\Application\Jobs\ProcessDeliveryWebhookJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryProviderEvent;
use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class DeliveryWebhookController extends Controller
{
    public function __construct(
        private readonly DeliveryAdapterRegistry $registry,
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    public function __invoke(Request $request, string $channel): JsonResponse
    {
        $rawBody = $request->getContent();
        $headers = $this->lowerCasedHeaders($request);
        $payload = json_decode($rawBody, true);
        $payload = is_array($payload) ? $payload : [];

        $externalStoreId = (string) ($payload['external_store_id'] ?? '');
        $externalOrderId = $payload['external_order_id'] ?? null;
        $externalEventId = (string) ($payload['event_id'] ?? hash('sha256', $rawBody));
        $eventType = (string) ($payload['event_type'] ?? 'order_create');

        // Resolve channel config (global read — webhooks carry no tenant context).
        /** @var DeliveryChannelConfig|null $config */
        $config = $this->withoutRowSecurity(fn () => DeliveryChannelConfig::query()
            ->where('channel_key', $channel)
            ->where('external_store_id', $externalStoreId)
            ->where('is_enabled', true)
            ->first());

        $adapter = $this->registry->forChannel($channel);

        $signatureValid = $config !== null
            && $adapter->verifyWebhookSignature($config->toArray(), $rawBody, $headers);

        $event = $this->persistEvent(
            $config,
            $channel,
            $externalEventId,
            $externalOrderId,
            $eventType,
            $signatureValid,
            $payload,
        );

        if (! $signatureValid) {
            $this->openExceptionCase->handle(
                merchantId: $config?->merchant_id,
                storeId: $config?->store_id,
                module: 'delivery',
                code: 'webhook_signature_invalid',
                severity: 'high',
                title: 'Delivery webhook failed signature verification.',
                details: [
                    'channel_key' => $channel,
                    'external_store_id' => $externalStoreId,
                ],
                relatedType: 'delivery_provider_event',
                relatedId: $event->id,
            );

            return response()->json(['message' => 'Invalid signature.'], 401);
        }

        ProcessDeliveryWebhookJob::dispatch($event->id);

        return response()->json(['status' => 'accepted'], 200);
    }

    private function persistEvent(
        ?DeliveryChannelConfig $config,
        string $channel,
        string $externalEventId,
        ?string $externalOrderId,
        string $eventType,
        bool $signatureValid,
        array $payload,
    ): DeliveryProviderEvent {
        return $this->withoutRowSecurity(fn () => DeliveryProviderEvent::query()->updateOrCreate(
            ['channel_key' => $channel, 'external_event_id' => $externalEventId],
            [
                'merchant_id' => $config?->merchant_id,
                'store_id' => $config?->store_id,
                'external_order_id' => $externalOrderId,
                'event_type' => $eventType,
                'signature_valid' => $signatureValid,
                'payload' => $payload,
            ],
        ));
    }

    /**
     * @return array<string, string>
     */
    private function lowerCasedHeaders(Request $request): array
    {
        $headers = [];
        foreach ($request->headers->all() as $name => $values) {
            $headers[strtolower($name)] = is_array($values) ? (string) ($values[0] ?? '') : (string) $values;
        }

        return $headers;
    }

    /**
     * @template TValue
     *
     * @param  callable(): TValue  $callback
     * @return TValue
     */
    private function withoutRowSecurity(callable $callback): mixed
    {
        if (DB::connection()->getDriverName() !== 'pgsql') {
            return $callback();
        }

        DB::unprepared('SET row_security = off');

        try {
            return $callback();
        } finally {
            DB::unprepared('SET row_security = on');
        }
    }
}
