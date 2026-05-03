<?php

namespace App\Modules\StoredValue\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;

class MembershipLedgerEntry extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'metadata' => 'array',
            'occurred_at' => 'immutable_datetime',
            'effective_starts_at' => 'immutable_datetime',
            'effective_ends_at' => 'immutable_datetime',
        ];
    }
}
