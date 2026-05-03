<?php

namespace App\Modules\OfflineSync\Application\Jobs;

use App\Modules\OfflineSync\Application\Actions\RecoverSyncEvent;
use Illuminate\Bus\Batchable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Queue\Queueable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;

class ReplaySyncEvent implements ShouldQueue
{
    use Batchable;
    use InteractsWithQueue;
    use Queueable;
    use SerializesModels;

    public int $tries = 1;

    public function __construct(
        public readonly string $syncEventId,
        public readonly string $syncRecoveryRunId,
    ) {}

    public function handle(RecoverSyncEvent $action): void
    {
        if ($this->batch()?->cancelled()) {
            return;
        }

        $action->handle($this->syncEventId);
    }
}
