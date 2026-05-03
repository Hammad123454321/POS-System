<?php

namespace App\Modules\ExceptionQueue\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;

class ExceptionCase extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'details' => 'array',
            'resolved_at' => 'immutable_datetime',
        ];
    }

    public function resolution(): HasOne
    {
        return $this->hasOne(ExceptionResolution::class);
    }
}
