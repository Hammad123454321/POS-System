<?php

namespace App\Modules\Catalog\Application\Items;

use App\Modules\Catalog\Domain\Models\CatalogItem;

class DeactivateCatalogItem
{
    public function handle(CatalogItem $item): CatalogItem
    {
        $item->forceFill(['is_active' => false])->save();

        return $item->refresh();
    }
}
