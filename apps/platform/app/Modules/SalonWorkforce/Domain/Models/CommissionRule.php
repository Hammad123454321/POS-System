<?php

namespace App\Modules\SalonWorkforce\Domain\Models;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class CommissionRule extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'rate_basis_points' => 'int',
            'fixed_minor' => 'int',
            'effective_from' => 'date:Y-m-d',
            'effective_to' => 'date:Y-m-d',
            'is_active' => 'bool',
        ];
    }

    public function store(): BelongsTo
    {
        return $this->belongsTo(Store::class);
    }
}
