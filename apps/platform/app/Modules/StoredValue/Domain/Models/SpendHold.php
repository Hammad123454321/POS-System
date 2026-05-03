<?php

namespace App\Modules\StoredValue\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;

class SpendHold extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'metadata' => 'array',
            'heartbeat_at' => 'immutable_datetime',
            'expires_at' => 'immutable_datetime',
        ];
    }
}
