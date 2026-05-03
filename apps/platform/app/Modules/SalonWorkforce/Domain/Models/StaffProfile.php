<?php

namespace App\Modules\SalonWorkforce\Domain\Models;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class StaffProfile extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'hired_on' => 'date:Y-m-d',
            'terminated_on' => 'date:Y-m-d',
            'metadata' => 'array',
        ];
    }

    public function store(): BelongsTo
    {
        return $this->belongsTo(Store::class);
    }

    public function wageRules(): HasMany
    {
        return $this->hasMany(WageRule::class);
    }

    public function staffServiceRules(): HasMany
    {
        return $this->hasMany(StaffServiceRule::class);
    }
}
