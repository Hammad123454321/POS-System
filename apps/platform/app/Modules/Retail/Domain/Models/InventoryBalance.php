<?php

namespace App\Modules\Retail\Domain\Models;

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class InventoryBalance extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'on_hand_quantity' => 'int',
            'reserved_quantity' => 'int',
            'available_quantity' => 'int',
            'inventory_ledger_seq' => 'int',
            'last_count_closed_at' => 'immutable_datetime',
        ];
    }

    public function catalogItem(): BelongsTo
    {
        return $this->belongsTo(CatalogItem::class);
    }
}
