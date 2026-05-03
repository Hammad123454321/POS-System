<?php

namespace App\Modules\Payments\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;

class PaymentProviderEvent extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'signature_valid' => 'bool',
            'payload' => 'array',
            'occurred_at' => 'immutable_datetime',
        ];
    }
}
