<?php

namespace App\Modules\PlatformCore\Domain\Models;

use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Merchant extends Model
{
    use HasUlids;

    protected $guarded = [];

    public function stores(): HasMany
    {
        return $this->hasMany(Store::class);
    }

    public function devices(): HasMany
    {
        return $this->hasMany(Device::class);
    }
}
