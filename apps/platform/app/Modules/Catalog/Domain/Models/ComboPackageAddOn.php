<?php

namespace App\Modules\Catalog\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ComboPackageAddOn extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    public function comboPackage(): BelongsTo
    {
        return $this->belongsTo(ComboPackage::class, 'combo_package_id');
    }

    public function catalogItem(): BelongsTo
    {
        return $this->belongsTo(CatalogItem::class, 'catalog_item_id');
    }
}
