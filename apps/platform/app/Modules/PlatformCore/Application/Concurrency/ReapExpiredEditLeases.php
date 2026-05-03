<?php

namespace App\Modules\PlatformCore\Application\Concurrency;

use App\Modules\PlatformCore\Domain\Models\EditLease;
use Carbon\CarbonImmutable;
use Illuminate\Support\Collection;
use Illuminate\Support\Facades\DB;

class ReapExpiredEditLeases
{
    public function handle(): int
    {
        $leases = EditLease::query()
            ->whereNotNull('holder_device_id')
            ->with('holderDevice')
            ->get();

        /** @var Collection<int, EditLease> $expired */
        $expired = $leases->filter(function (EditLease $lease): bool {
            $holderIsInactive = $lease->holderDevice !== null
                && $lease->holderDevice->status !== 'active';

            return $holderIsInactive
                || ($lease->lease_expires_at !== null && $lease->lease_expires_at->isPast());
        });

        foreach ($expired as $lease) {
            DB::transaction(function () use ($lease): void {
                $freshLease = EditLease::query()
                    ->whereKey($lease->id)
                    ->lockForUpdate()
                    ->first();

                if ($freshLease === null || $freshLease->holder_device_id === null) {
                    return;
                }

                $freshLease->forceFill([
                    'holder_device_id' => null,
                    'lease_version' => $freshLease->lease_version + 1,
                    'lease_expired_at' => CarbonImmutable::now('UTC'),
                    'last_heartbeat_at' => null,
                    'lease_expires_at' => null,
                ])->save();
            });
        }

        return $expired->count();
    }
}
