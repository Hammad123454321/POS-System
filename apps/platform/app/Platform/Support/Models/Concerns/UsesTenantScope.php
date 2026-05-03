<?php

namespace App\Platform\Support\Models\Concerns;

use Illuminate\Database\Eloquent\Builder;

trait UsesTenantScope
{
    public function scopeForMerchant(Builder $query, string $merchantId): Builder
    {
        return $query->where($this->qualifyColumn('merchant_id'), $merchantId);
    }
}
