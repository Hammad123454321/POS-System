<?php

namespace App\Modules\OrderRegister\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Payment extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'applied_minor' => 'int',
            'tip_minor' => 'int',
            'refundable_minor' => 'int',
            'authorized_at' => 'immutable_datetime',
            'captured_at' => 'immutable_datetime',
            'voided_at' => 'immutable_datetime',
            'refunded_at' => 'immutable_datetime',
            'metadata' => 'array',
        ];
    }

    public function order(): BelongsTo
    {
        return $this->belongsTo(Order::class);
    }

    public function refunds(): HasMany
    {
        return $this->hasMany(Refund::class);
    }

    public function voidRecords(): HasMany
    {
        return $this->hasMany(VoidRecord::class);
    }

    public function splits(): HasMany
    {
        return $this->hasMany(PaymentSplit::class);
    }
}
