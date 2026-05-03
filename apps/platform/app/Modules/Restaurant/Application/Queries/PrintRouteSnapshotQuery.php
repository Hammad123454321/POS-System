<?php

namespace App\Modules\Restaurant\Application\Queries;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Restaurant\Domain\Models\PrintRoute;

class PrintRouteSnapshotQuery
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function forDevice(Device $device): array
    {
        return PrintRoute::query()
            ->forMerchant($device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('is_active', true)
            ->with(['primaryPrinter', 'secondaryPrinter'])
            ->orderBy('route_key')
            ->get()
            ->map(fn (PrintRoute $route): array => [
                'id' => $route->id,
                'route_key' => $route->route_key,
                'document_type' => $route->document_type,
                'primary_printer' => [
                    'id' => $route->primaryPrinter?->id,
                    'name' => $route->primaryPrinter?->name,
                    'driver_key' => $route->primaryPrinter?->driver_key,
                ],
                'secondary_printer' => $route->secondaryPrinter === null ? null : [
                    'id' => $route->secondaryPrinter->id,
                    'name' => $route->secondaryPrinter->name,
                    'driver_key' => $route->secondaryPrinter->driver_key,
                ],
            ])
            ->values()
            ->all();
    }
}
