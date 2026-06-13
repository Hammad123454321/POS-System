<?php

namespace App\Modules\Reporting\Application\Jobs;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Reporting\Application\Actions\RefreshDailyStoreSummary;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;

class RefreshDailyStoreSummaryJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public function __construct(
        public readonly string $storeId,
        public readonly string $businessDate,
        public readonly bool $final = false,
    ) {
        $this->onQueue('reporting');
    }

    public function handle(RefreshDailyStoreSummary $action): void
    {
        $store = Store::query()->find($this->storeId);

        if ($store === null) {
            return;
        }

        $action->handle($store, $this->businessDate, $this->final);
    }
}
