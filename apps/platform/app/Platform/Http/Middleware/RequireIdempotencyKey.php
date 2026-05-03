<?php

namespace App\Platform\Http\Middleware;

use App\Modules\OfflineSync\Domain\Models\IdempotencyRecord;
use App\Modules\PlatformCore\Domain\Models\Device;
use Closure;
use Illuminate\Contracts\Auth\Authenticatable;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Http\Response as HttpResponse;
use Illuminate\Support\Carbon;
use Symfony\Component\HttpFoundation\Response;

class RequireIdempotencyKey
{
    public function handle(Request $request, Closure $next): Response
    {
        $key = $request->header('Idempotency-Key');

        if ($key === null || $key === '') {
            return new JsonResponse([
                'message' => 'Missing required header: Idempotency-Key.',
            ], HttpResponse::HTTP_BAD_REQUEST);
        }

        $scope = $this->scopeFromRequest($request);

        if ($scope === null) {
            return new JsonResponse([
                'message' => 'Unable to resolve idempotency scope for this request.',
            ], HttpResponse::HTTP_FORBIDDEN);
        }

        $routeKey = $request->route()?->getName() ?? $request->route()?->uri() ?? $request->path();
        $requestHash = hash('sha256', $request->getContent());

        $record = IdempotencyRecord::query()
            ->where('scope_type', $scope['type'])
            ->where('scope_id', $scope['id'])
            ->where('request_method', $request->method())
            ->where('route_key', $routeKey)
            ->where('idempotency_key', $key)
            ->where('expires_at', '>', Carbon::now())
            ->first();

        if ($record !== null) {
            if ($record->request_hash !== $requestHash) {
                return new JsonResponse([
                    'message' => 'Idempotency key reuse with a different payload is not allowed.',
                ], HttpResponse::HTTP_UNPROCESSABLE_ENTITY);
            }

            return new JsonResponse(
                $record->response_body ?? [],
                $record->response_status,
                $record->response_headers ?? [],
            );
        }

        /** @var Response $response */
        $response = $next($request);

        if ($response->getStatusCode() < 500) {
            IdempotencyRecord::query()->create([
                'scope_type' => $scope['type'],
                'scope_id' => $scope['id'],
                'request_method' => $request->method(),
                'route_key' => $routeKey,
                'idempotency_key' => $key,
                'request_hash' => $requestHash,
                'response_status' => $response->getStatusCode(),
                'response_headers' => $response->headers->allPreserveCaseWithoutCookies(),
                'response_body' => $this->extractResponseBody($response),
                'expires_at' => Carbon::now()->addHours((int) config('pos.idempotency.ttl_hours', 72)),
            ]);
        }

        return $response;
    }

    /**
     * @return array{type: string, id: string}|null
     */
    private function scopeFromRequest(Request $request): ?array
    {
        $actor = $request->user();

        if ($actor instanceof Device) {
            return ['type' => 'device', 'id' => $actor->id];
        }

        if ($actor instanceof Authenticatable && method_exists($actor, 'getAuthIdentifier')) {
            return ['type' => 'user', 'id' => (string) $actor->getAuthIdentifier()];
        }

        return null;
    }

    /**
     * @return array<string, mixed>|string|null
     */
    private function extractResponseBody(Response $response): array|string|null
    {
        if ($response instanceof JsonResponse) {
            return $response->getData(true);
        }

        $content = $response->getContent();

        if ($content === false || $content === '') {
            return null;
        }

        $decoded = json_decode($content, true);

        return json_last_error() === JSON_ERROR_NONE ? $decoded : $content;
    }
}
