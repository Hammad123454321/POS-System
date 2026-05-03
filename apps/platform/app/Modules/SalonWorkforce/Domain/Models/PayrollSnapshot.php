<?php

namespace App\Modules\SalonWorkforce\Domain\Models;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class PayrollSnapshot extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'period_start' => 'date:Y-m-d',
            'period_end' => 'date:Y-m-d',
            'staff_count' => 'int',
            'approved_minutes' => 'int',
            'regular_minutes' => 'int',
            'overtime_minutes' => 'int',
            'gross_wages_minor' => 'int',
            'gross_commission_minor' => 'int',
            'gross_pay_minor' => 'int',
            'payload' => 'array',
            'generated_at' => 'immutable_datetime',
        ];
    }

    public function store(): BelongsTo
    {
        return $this->belongsTo(Store::class);
    }
}
