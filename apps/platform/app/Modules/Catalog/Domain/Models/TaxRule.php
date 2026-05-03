<?php

namespace App\Modules\Catalog\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class TaxRule extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'is_inclusive' => 'bool',
            'is_active' => 'bool',
        ];
    }

    public function items(): HasMany
    {
        return $this->hasMany(CatalogItem::class);
    }
}
