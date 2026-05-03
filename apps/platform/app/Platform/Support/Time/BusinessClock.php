<?php

namespace App\Platform\Support\Time;

use App\Modules\PlatformCore\Domain\Models\Store;
use Carbon\CarbonImmutable;

final class BusinessClock
{
    public function businessDateForStore(Store $store, ?CarbonImmutable $moment = null): string
    {
        $utcMoment = $moment ?? CarbonImmutable::now('UTC');
        $localMoment = $utcMoment->setTimezone($store->timezone);

        [$cutoffHour, $cutoffMinute] = array_map(
            static fn (string $value): int => (int) $value,
            explode(':', $store->business_day_cutoff),
        );

        $cutoff = $localMoment->setTime($cutoffHour, $cutoffMinute);

        if ($localMoment->lt($cutoff)) {
            return $localMoment->subDay()->toDateString();
        }

        return $localMoment->toDateString();
    }
}
