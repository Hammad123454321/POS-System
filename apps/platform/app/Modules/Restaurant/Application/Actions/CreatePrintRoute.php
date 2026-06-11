<?php

namespace App\Modules\Restaurant\Application\Actions;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Restaurant\Domain\Models\PrinterConfig;
use App\Modules\Restaurant\Domain\Models\PrintRoute;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class CreatePrintRoute
{
    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $routeKey,
        string $documentType,
        string $primaryPrinterConfigId,
        ?string $secondaryPrinterConfigId,
    ): array {
        if (! $this->canManageRestaurantConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage print routes for this store.');
        }

        $primaryPrinter = PrinterConfig::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereKey($primaryPrinterConfigId)
            ->where('is_active', true)
            ->firstOrFail();

        $secondaryPrinter = $secondaryPrinterConfigId === null ? null : PrinterConfig::query()
            ->where('merchant_id', $store->merchant_id)
            ->where('store_id', $store->id)
            ->whereKey($secondaryPrinterConfigId)
            ->where('is_active', true)
            ->firstOrFail();

        $route = PrintRoute::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'route_key' => $routeKey,
            'document_type' => $documentType,
            'primary_printer_config_id' => $primaryPrinter->id,
            'secondary_printer_config_id' => $secondaryPrinter?->id,
            'is_active' => true,
        ]);

        return [
            'id' => $route->id,
            'route_key' => $route->route_key,
            'document_type' => $route->document_type,
            'primary_printer' => [
                'id' => $primaryPrinter->id,
                'name' => $primaryPrinter->name,
            ],
            'secondary_printer' => $secondaryPrinter === null ? null : [
                'id' => $secondaryPrinter->id,
                'name' => $secondaryPrinter->name,
            ],
        ];
    }

    private function canManageRestaurantConfig(User $actor, Store $store): bool
    {
        if ($actor->is_super_admin) {
            return true;
        }

        return DB::table('user_store_role')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->where('user_store_role.user_id', $actor->id)
            ->where('user_store_role.store_id', $store->id)
            ->whereIn('roles.name', ['Merchant Owner', 'Store Admin'])
            ->exists();
    }
}
