<?php

namespace App\Console\Commands;

use App\Modules\PlatformCore\Application\Concurrency\ReapExpiredEditLeases;
use Illuminate\Console\Command;

class ReapEditLeasesCommand extends Command
{
    protected $signature = 'pos:reap-edit-leases';

    protected $description = 'Release expired edit leases without mutating business records.';

    public function handle(ReapExpiredEditLeases $reaper): int
    {
        $count = $reaper->handle();

        $this->info("Reaped {$count} expired edit lease(s).");

        return self::SUCCESS;
    }
}
