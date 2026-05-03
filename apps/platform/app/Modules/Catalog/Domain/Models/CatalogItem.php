<?php

namespace App\Modules\Catalog\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class CatalogItem extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'is_active' => 'bool',
            'sold_out' => 'bool',
        ];
    }

    public function category(): BelongsTo
    {
        return $this->belongsTo(Category::class);
    }

    public function taxRule(): BelongsTo
    {
        return $this->belongsTo(TaxRule::class);
    }

    public function priceRules(): HasMany
    {
        return $this->hasMany(PriceRule::class);
    }

    public function variants(): HasMany
    {
        return $this->hasMany(Variant::class, 'catalog_item_id');
    }

    public function modifierGroups(): HasMany
    {
        return $this->hasMany(ModifierGroup::class, 'catalog_item_id');
    }

    public function addOnMappings(): HasMany
    {
        return $this->hasMany(CatalogItemAddOn::class, 'catalog_item_id');
    }
}
