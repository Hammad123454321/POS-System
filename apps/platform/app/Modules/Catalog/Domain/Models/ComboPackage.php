<?php

namespace App\Modules\Catalog\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class ComboPackage extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'is_active' => 'bool',
        ];
    }

    public function items(): HasMany
    {
        return $this->hasMany(ComboPackageItem::class, 'combo_package_id');
    }

    public function addOns(): HasMany
    {
        return $this->hasMany(ComboPackageAddOn::class, 'combo_package_id');
    }
}
