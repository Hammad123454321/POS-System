<?php

namespace App\Modules\StoredValue\Domain\Models;

use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class GiftCard extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'activated_at' => 'immutable_datetime',
            'last_used_at' => 'immutable_datetime',
        ];
    }

    public function issuedToCustomer(): BelongsTo
    {
        return $this->belongsTo(Customer::class, 'issued_to_customer_id');
    }

    public function ledgerEntries(): HasMany
    {
        return $this->hasMany(GiftCardLedgerEntry::class);
    }
}
