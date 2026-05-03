<?php

namespace App\Modules\SalonWorkforce\Domain\Models;

use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Appointment extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'business_date' => 'date:Y-m-d',
            'starts_at' => 'immutable_datetime',
            'ends_at' => 'immutable_datetime',
            'checked_in_at' => 'immutable_datetime',
            'completed_at' => 'immutable_datetime',
            'cancelled_at' => 'immutable_datetime',
            'status_seq' => 'int',
            'service_price_minor' => 'int',
            'discount_minor' => 'int',
            'metadata' => 'array',
        ];
    }

    public function customer(): BelongsTo
    {
        return $this->belongsTo(Customer::class);
    }

    public function store(): BelongsTo
    {
        return $this->belongsTo(Store::class);
    }

    public function staffProfile(): BelongsTo
    {
        return $this->belongsTo(StaffProfile::class);
    }

    public function serviceItem(): BelongsTo
    {
        return $this->belongsTo(ServiceItem::class);
    }

    public function slotClaim(): BelongsTo
    {
        return $this->belongsTo(EditLease::class, 'slot_claim_id');
    }
}
