<?php

namespace App\Console\Commands;

use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Reporting\Application\Jobs\RefreshDailyStoreSummaryJob;
use App\Platform\Support\Time\BusinessClock;
use Carbon\CarbonImmutable;
use Illuminate\Console\Command;

class RefreshReportSummariesCommand extends Command
{
    protected $signature = 'pos:refresh-report-summaries {--finalize : Finalize the previous business day instead of refreshing today}';

    protected $description = 'Materialize daily store reporting summaries for all active stores.';

    public function handle(BusinessClock $businessClock): int
    {
        $finalize = (bool) $this->option('finalize');

        Store::query()->where('status', 'active')->chunkById(200, function ($stores) use ($businessClock, $finalize): void {
            foreach ($stores as $store) {
                $today = $businessClock->businessDateForStore($store, CarbonImmutable::now('UTC'));
                $businessDate = $finalize
                    ? CarbonImmutable::parse($today)->subDay()->toDateString()
                    : $today;

                RefreshDailyStoreSummaryJob::dispatch($store->id, $businessDate, $finalize);
            }
        });

        $this->info($finalize ? 'Dispatched finalization jobs.' : 'Dispatched incremental refresh jobs.');

        return self::SUCCESS;
    }
}
