<?php

namespace App\Modules\Restaurant\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;

class DiningTable extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    public function assignment(): HasOne
    {
        return $this->hasOne(TableAssignment::class);
    }
}
