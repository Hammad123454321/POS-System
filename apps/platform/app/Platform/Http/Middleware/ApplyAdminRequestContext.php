<?php

namespace App\Platform\Http\Middleware;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Context\TenantContext;
use App\Platform\Support\Reporting\ReportingConnection;
use Closure;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpFoundation\Response;

class ApplyAdminRequestContext
{
    public function __construct(
        private readonly ReportingConnection $reportingConnection,
    ) {}

    public function handle(Request $request, Closure $next): Response
    {
        $user = $request->user();

        if (! $user instanceof User) {
            return new JsonResponse(['message' => 'Unauthenticated.'], Response::HTTP_UNAUTHORIZED);
        }

        [$merchant, $store] = $this->resolveTenant($request);

        if ($merchant !== null && $merchant->status !== 'active' && ! $user->is_super_admin) {
            return new JsonResponse(['message' => 'This merchant is suspended.'], Response::HTTP_FORBIDDEN);
        }

        if ($store !== null && $store->status !== 'active' && ! $user->is_super_admin) {
            return new JsonResponse(['message' => 'This store is suspended.'], Response::HTTP_FORBIDDEN);
        }

        if (! $user->is_super_admin) {
            if ($store !== null && ! $user->accessibleStoreIds()->contains($store->id)) {
                return new JsonResponse(['message' => 'You are not allowed to access this store.'], Response::HTTP_FORBIDDEN);
            }

            if ($store === null && $merchant !== null && ! $user->accessibleMerchantIds()->contains($merchant->id)) {
                return new JsonResponse(['message' => 'You are not allowed to access this merchant.'], Response::HTTP_FORBIDDEN);
            }
        }

        $merchantId = $store?->merchant_id ?? $merchant?->id;
        $storeId = $store?->id;

        app()->instance(TenantContext::class, new TenantContext(
            merchantId: $merchantId,
            storeId: $storeId,
        ));

        $request->attributes->set('merchant_id', $merchantId);
        $request->attributes->set('store_id', $storeId);

        $this->setTenantSettings($merchantId, $storeId);

        return $next($request);
    }

    /**
     * @return array{0: Merchant|null, 1: Store|null}
     */
    private function resolveTenant(Request $request): array
    {
        $store = $request->route('store');

        if ($store instanceof Store) {
            $store->loadMissing('merchant');

            return [$store->merchant, $store];
        }

        if (is_string($store)) {
            $store = $this->withoutRowSecurity(fn () => Store::query()->with('merchant')->find($store));

            return [$store?->merchant, $store];
        }

        $merchant = $request->route('merchant');

        if ($merchant instanceof Merchant) {
            return [$merchant, null];
        }

        if (is_string($merchant)) {
            return [$this->withoutRowSecurity(fn () => Merchant::query()->find($merchant)), null];
        }

        return [null, null];
    }

    private function setTenantSettings(?string $merchantId, ?string $storeId): void
    {
        if ($merchantId === null || DB::connection()->getDriverName() !== 'pgsql') {
            return;
        }

        DB::statement('select set_config(?, ?, false)', ['app.current_merchant_id', $merchantId]);
        DB::statement('select set_config(?, ?, false)', ['app.current_store_id', $storeId ?? '']);

        $reportingConnection = $this->reportingConnection->name();

        if ($reportingConnection !== DB::connection()->getName() && DB::connection($reportingConnection)->getDriverName() === 'pgsql') {
            DB::connection($reportingConnection)->statement('select set_config(?, ?, false)', ['app.current_merchant_id', $merchantId]);
            DB::connection($reportingConnection)->statement('select set_config(?, ?, false)', ['app.current_store_id', $storeId ?? '']);
        }
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

        DB::statement('set row_security = off');

        try {
            return $callback();
        } finally {
            DB::statement('set row_security = on');
        }
    }
}
