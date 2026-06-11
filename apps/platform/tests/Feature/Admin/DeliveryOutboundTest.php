<?php

use App\Modules\DeliveryIntegrations\Application\Adapters\UberEatsAdapter;
use App\Modules\DeliveryIntegrations\Application\Exceptions\DeliveryTransportException;
use Illuminate\Support\Facades\Http;

it('does not make HTTP calls in sandbox mode', function () {
    config()->set('pos.delivery_channels.sandbox', true);
    config()->set('pos.delivery_channels.uber_eats.sandbox_base_url', '');
    Http::fake();

    $adapter = app(UberEatsAdapter::class);
    $result = $adapter->publishMenu(['credentials' => []], ['items' => []]);

    expect($result['transport'])->toBe('stub');
    Http::assertNothingSent();
});

it('sends a real request with the transformed body when a base URL is configured', function () {
    config()->set('pos.delivery_channels.sandbox', false);
    config()->set('pos.delivery_channels.uber_eats.base_url', 'https://api.uber.test');

    Http::fake(['*' => Http::response(['ok' => true], 200)]);

    $adapter = app(UberEatsAdapter::class);
    $result = $adapter->publishMenu(
        ['credentials' => ['access_token' => 'tok']],
        ['items' => [['name' => 'Burger']]],
    );

    expect($result['transport'])->toBe('http');
    Http::assertSent(fn ($request) => str_contains($request->url(), '/v1/eats/stores/menu')
        && $request->hasHeader('Authorization', 'Bearer tok'));
});

it('throws a transport exception after retries on persistent failure', function () {
    config()->set('pos.delivery_channels.sandbox', false);
    config()->set('pos.delivery_channels.uber_eats.base_url', 'https://api.uber.test');
    config()->set('pos.delivery_channels.retries', 2);
    config()->set('pos.delivery_channels.retry_backoff_ms', 1);

    Http::fake(['*' => Http::response(['error' => 'boom'], 500)]);

    $adapter = app(UberEatsAdapter::class);

    expect(fn () => $adapter->publishMenu(['credentials' => []], ['items' => []]))
        ->toThrow(DeliveryTransportException::class);
});
