<?php

namespace App\Modules\Catalog\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Category extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    public function items(): HasMany
    {
        return $this->hasMany(CatalogItem::class);
    }
}
