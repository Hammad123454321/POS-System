<?php

namespace App\Modules\OrderRegister\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class RegisterSession extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'opened_at' => 'immutable_datetime',
            'closed_at' => 'immutable_datetime',
            'business_date' => 'date:Y-m-d',
        ];
    }

    public function orders(): HasMany
    {
        return $this->hasMany(Order::class);
    }

    public function cashMovements(): HasMany
    {
        return $this->hasMany(CashMovement::class);
    }
}
