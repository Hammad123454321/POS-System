<?php

namespace App\Modules\Retail\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class InventoryAdjustment extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'quantity_delta' => 'int',
            'quantity_before' => 'int',
            'quantity_after' => 'int',
            'count_closed_at' => 'immutable_datetime',
            'metadata' => 'array',
        ];
    }

    public function inventoryBalance(): BelongsTo
    {
        return $this->belongsTo(InventoryBalance::class);
    }
}
