<?php

namespace App\Modules\PlatformCore\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Auth\Authenticatable;
use Illuminate\Contracts\Auth\Authenticatable as AuthenticatableContract;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Laravel\Sanctum\HasApiTokens;

class Device extends Model implements AuthenticatableContract
{
    use Authenticatable;
    use HasApiTokens;
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];
    protected $keyType = 'string';
    public $incrementing = false;

    protected function casts(): array
    {
        return [
            'attestation_payload' => 'array',
            'enrolled_at' => 'immutable_datetime',
            'last_authenticated_at' => 'immutable_datetime',
            'last_seen_at' => 'immutable_datetime',
        ];
    }

    public function merchant(): BelongsTo
    {
        return $this->belongsTo(Merchant::class);
    }

    public function store(): BelongsTo
    {
        return $this->belongsTo(Store::class);
    }

    public function profile(): BelongsTo
    {
        return $this->belongsTo(DeviceProfile::class, 'device_profile_id');
    }

    public function refreshTokens(): HasMany
    {
        return $this->hasMany(DeviceRefreshToken::class);
    }
}
