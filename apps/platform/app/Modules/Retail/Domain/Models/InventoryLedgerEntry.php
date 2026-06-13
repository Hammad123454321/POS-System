<?php

namespace App\Modules\Retail\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;

/**
 * Append-only inventory movement ledger. One row per stock-changing event,
 * monotonically sequenced per (store, sku). Balances are derivable by summing
 * delta_quantity; the ledger is the source of truth for sync arbitration of
 * absolute on-hand "set" events.
 */
class InventoryLedgerEntry extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'seq' => 'int',
            'delta_quantity' => 'int',
            'occurred_at' => 'immutable_datetime',
        ];
    }
}
