<?php

namespace App\Modules\DeliveryIntegrations\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;

class DeliveryChannelConfig extends Model
{
    use HasUlids;
    use UsesTenantScope;

    protected $guarded = [];

    protected function casts(): array
    {
        return [
            'is_enabled' => 'bool',
            'credentials' => 'array',
            'mapping' => 'array',
            'metadata' => 'array',
            'pause_windows' => 'array',
            'sync_hours_enabled' => 'bool',
            'sync_prep_time_enabled' => 'bool',
            'sync_menu_enabled' => 'bool',
            'last_menu_published_at' => 'immutable_datetime',
        ];
    }
}
