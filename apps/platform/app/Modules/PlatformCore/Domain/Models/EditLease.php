<?php

namespace App\Modules\PlatformCore\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class EditLease extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'last_heartbeat_at' => 'immutable_datetime',
            'lease_expires_at' => 'immutable_datetime',
            'lease_expired_at' => 'immutable_datetime',
        ];
    }

    public function holderDevice(): BelongsTo
    {
        return $this->belongsTo(Device::class, 'holder_device_id');
    }
}
