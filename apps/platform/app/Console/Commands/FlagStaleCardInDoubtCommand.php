<?php

namespace App\Console\Commands;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use Carbon\CarbonImmutable;
use Illuminate\Console\Command;

class FlagStaleCardInDoubtCommand extends Command
{
    protected $signature = 'pos:payments:flag-stale-in-doubt';

    protected $description = 'Escalate unresolved card in-doubt exceptions after the stale threshold.';

    public function handle(OpenExceptionCase $openExceptionCase): int
    {
        $thresholdMinutes = max(1, (int) config('pos.payments.in_doubt_stale_minutes', 15));
        $cutoff = CarbonImmutable::now('UTC')->subMinutes($thresholdMinutes);

        $inDoubtCases = ExceptionCase::query()
            ->where('module', 'payments')
            ->where('code', 'card_in_doubt')
            ->where('status', 'open')
            ->where('created_at', '<=', $cutoff)
            ->get();

        $created = 0;

        foreach ($inDoubtCases as $inDoubtCase) {
            $existingEscalation = ExceptionCase::query()
                ->where('module', 'payments')
                ->where('code', 'stale_in_doubt_record')
                ->where('status', 'open')
                ->where('related_type', 'order')
                ->where('related_id', $inDoubtCase->related_id)
                ->first();

            if ($existingEscalation !== null) {
                continue;
            }

            $openExceptionCase->handle(
                merchantId: $inDoubtCase->merchant_id,
                storeId: $inDoubtCase->store_id,
                module: 'payments',
                code: 'stale_in_doubt_record',
                severity: 'high',
                title: 'Card in-doubt transaction is stale and still unresolved.',
                details: [
                    'source_exception_case_id' => $inDoubtCase->id,
                    'threshold_minutes' => $thresholdMinutes,
                    'opened_at' => $inDoubtCase->created_at?->toIso8601String(),
                ],
                relatedType: 'order',
                relatedId: $inDoubtCase->related_id,
                openedByDeviceId: null,
                openedByUserId: null,
            );
            $created++;
        }

        $this->info("Created {$created} stale in-doubt exception case(s).");

        return self::SUCCESS;
    }
}
