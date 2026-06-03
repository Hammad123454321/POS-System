<?php

namespace App\Platform\Http\Middleware;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Platform\Support\Context\TenantContext;
use Closure;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class ApplyPosRequestContext
{
    public function handle(Request $request, Closure $next): Response
    {
        $requestId = $request->header('X-Request-Id', (string) Str::uuid());
        $traceId = $request->header('X-Trace-Id', (string) Str::uuid());
        $device = $request->user();

        $merchantId = null;
        $storeId = null;
        $deviceId = null;

        if ($device instanceof Device) {
            $device->loadMissing('store.merchant');

            if (
                $device->status !== 'active'
                || $device->store?->status !== 'active'
                || $device->store?->merchant?->status !== 'active'
            ) {
                return new JsonResponse([
                    'message' => 'This device is no longer permitted to access POS APIs.',
                    'upgrade_required' => false,
                ], Response::HTTP_FORBIDDEN);
            }

            $merchantId = $device->merchant_id;
            $storeId = $device->store_id;
            $deviceId = $device->id;

            if ($device->last_seen_at === null || $device->last_seen_at->lt(Carbon::now('UTC')->subMinute())) {
                $device->forceFill([
                    'last_seen_at' => Carbon::now('UTC'),
                ])->saveQuietly();
            }
        }

        app()->instance(TenantContext::class, new TenantContext(
            merchantId: $merchantId,
            storeId: $storeId,
            deviceId: $deviceId,
        ));

        $request->attributes->set('merchant_id', $merchantId);
        $request->attributes->set('store_id', $storeId);
        $request->attributes->set('device_id', $deviceId);

        if ($merchantId !== null && DB::connection()->getDriverName() === 'pgsql') {
            DB::statement('select set_config(?, ?, false)', ['app.current_merchant_id', $merchantId]);
            DB::statement('select set_config(?, ?, false)', ['app.current_store_id', $storeId ?? '']);
            DB::statement('select set_config(?, ?, false)', ['app.current_device_id', $deviceId ?? '']);
        }

        Log::shareContext([
            'trace_id' => $traceId,
            'request_id' => $requestId,
            'tenant_id' => $merchantId,
            'store_id' => $storeId,
            'device_id' => $deviceId,
            'user_id' => null,
            'module' => 'pos_api',
            'route' => $request->route()?->uri(),
            'api_version' => 'v'.($request->attributes->get('pos_api_major') ?? $request->route('major') ?? 1),
            'app_version' => $request->header('X-POS-App-Version'),
            'register_session_id' => $request->input('register_session_id'),
        ]);

        $response = $next($request);

        $response->headers->set('Request-Id', $requestId);
        $response->headers->set('Trace-Id', $traceId);

        return $response;
    }
}
