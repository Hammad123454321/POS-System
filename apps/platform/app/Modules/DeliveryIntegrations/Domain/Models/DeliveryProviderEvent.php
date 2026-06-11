<?php

namespace App\Modules\DeliveryIntegrations\Domain\Models;

use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;

class DeliveryProviderEvent extends Model
{
    use HasUlids;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'payload' => 'array',
            'signature_valid' => 'bool',
            'processed_at' => 'immutable_datetime',
        ];
    }
}
