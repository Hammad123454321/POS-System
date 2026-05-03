<?php

namespace App\Modules\OfflineSync\Domain\Models;

use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;

class IdempotencyRecord extends Model
{
    use HasUlids;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'response_headers' => 'array',
            'response_body' => 'array',
            'expires_at' => 'immutable_datetime',
        ];
    }
}
