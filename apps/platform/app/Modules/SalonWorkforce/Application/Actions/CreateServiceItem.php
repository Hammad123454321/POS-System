<?php

namespace App\Modules\SalonWorkforce\Application\Actions;

use App\Models\User;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Support\WorkforceAuthorization;
use App\Modules\SalonWorkforce\Domain\Models\ServiceItem;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class CreateServiceItem
{
    public function __construct(
        private readonly WorkforceAuthorization $authorization,
        private readonly AuditLogger $auditLogger,
        private readonly RecordUsage $recordUsage,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $name,
        ?string $sku,
        int $basePriceMinor,
        int $durationMinutes,
        int $bufferMinutes,
        bool $isWalkInEnabled,
    ): array {
        if (! $this->authorization->canManageStore($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage salon services for this store.');
        }

        return DB::transaction(function () use (
            $actor,
            $basePriceMinor,
            $bufferMinutes,
            $durationMinutes,
            $isWalkInEnabled,
            $name,
            $sku,
            $store,
        ): array {
            $currency = $store->merchant()->value('currency') ?? 'USD';

            $catalogItem = CatalogItem::query()->create([
                'merchant_id' => $store->merchant_id,
                'category_id' => null,
                'tax_rule_id' => null,
                'type' => 'service',
                'name' => $name,
                'sku' => $sku,
                'base_price_minor' => $basePriceMinor,
                'currency' => $currency,
                'is_active' => true,
                'sold_out' => false,
            ]);

            $serviceItem = ServiceItem::query()->create([
                'merchant_id' => $store->merchant_id,
                'catalog_item_id' => $catalogItem->id,
                'duration_minutes' => $durationMinutes,
                'buffer_minutes' => $bufferMinutes,
                'is_walk_in_enabled' => $isWalkInEnabled,
            ]);

            $this->auditLogger->log(
                $store->merchant_id,
                $store->id,
                'salon_workforce',
                'service_item.created',
                'service_item',
                $serviceItem->id,
                null,
                [
                    'catalog_item_id' => $catalogItem->id,
                    'name' => $catalogItem->name,
                    'duration_minutes' => $serviceItem->duration_minutes,
                ],
                null,
                $actor->id,
            );
            $this->recordUsage->handle($store->merchant_id, $store->id, 'salon.service_item.created');

            return [
                'id' => $serviceItem->id,
                'catalog_item_id' => $catalogItem->id,
                'name' => $catalogItem->name,
                'sku' => $catalogItem->sku,
                'base_price_minor' => $catalogItem->base_price_minor,
                'currency' => $catalogItem->currency,
                'duration_minutes' => $serviceItem->duration_minutes,
                'buffer_minutes' => $serviceItem->buffer_minutes,
                'is_walk_in_enabled' => $serviceItem->is_walk_in_enabled,
            ];
        });
    }
}
