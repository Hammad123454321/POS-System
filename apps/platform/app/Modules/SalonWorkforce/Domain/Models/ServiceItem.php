<?php

namespace App\Modules\SalonWorkforce\Domain\Models;

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class ServiceItem extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'duration_minutes' => 'int',
            'buffer_minutes' => 'int',
            'is_walk_in_enabled' => 'bool',
        ];
    }

    public function catalogItem(): BelongsTo
    {
        return $this->belongsTo(CatalogItem::class);
    }

    public function staffServiceRules(): HasMany
    {
        return $this->hasMany(StaffServiceRule::class);
    }
}
