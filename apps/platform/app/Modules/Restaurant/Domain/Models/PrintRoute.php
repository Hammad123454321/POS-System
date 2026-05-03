<?php

namespace App\Modules\Restaurant\Domain\Models;

use App\Platform\Support\Models\Concerns\UsesTenantScope;
use Illuminate\Database\Eloquent\Concerns\HasUlids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class PrintRoute extends Model
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

    public function primaryPrinter(): BelongsTo
    {
        return $this->belongsTo(PrinterConfig::class, 'primary_printer_config_id');
    }

    public function secondaryPrinter(): BelongsTo
    {
        return $this->belongsTo(PrinterConfig::class, 'secondary_printer_config_id');
    }
}
