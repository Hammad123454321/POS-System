<?php

namespace App\Modules\SalonWorkforce\Domain\Models;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class AttendanceRecord extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'check_in_at' => 'immutable_datetime',
            'check_out_at' => 'immutable_datetime',
            'approved_at' => 'immutable_datetime',
            'worked_minutes' => 'int',
            'break_minutes' => 'int',
            'business_date' => 'date:Y-m-d',
            'metadata' => 'array',
        ];
    }

    public function store(): BelongsTo
    {
        return $this->belongsTo(Store::class);
    }

    public function staffProfile(): BelongsTo
    {
        return $this->belongsTo(StaffProfile::class);
    }

    public function shift(): BelongsTo
    {
        return $this->belongsTo(Shift::class);
    }
}
