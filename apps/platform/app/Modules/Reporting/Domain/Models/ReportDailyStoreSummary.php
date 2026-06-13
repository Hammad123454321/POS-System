<?php

namespace App\Modules\Reporting\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;

class ReportDailyStoreSummary extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'business_date' => 'date:Y-m-d',
            'tender_breakdown' => 'array',
            'is_final' => 'bool',
            'last_aggregated_at' => 'immutable_datetime',
        ];
    }
}
