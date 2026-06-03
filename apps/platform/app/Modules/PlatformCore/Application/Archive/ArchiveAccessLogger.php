<?php

namespace App\Modules\PlatformCore\Application\Archive;

use App\Modules\PlatformCore\Domain\Models\ArchiveAccessLog;
use Illuminate\Support\Carbon;

class ArchiveAccessLogger
{
    /**
     * @param  array<string, mixed>  $metadata
     */
    public function log(
        ?string $merchantId,
        ?string $storeId,
        string $archiveType,
        string $archiveRecordId,
        ?int $userId,
        ?string $deviceId,
        ?string $reason,
        array $metadata = [],
    ): void {
        ArchiveAccessLog::query()->create([
            'merchant_id' => $merchantId,
            'store_id' => $storeId,
            'archive_type' => $archiveType,
            'archive_record_id' => $archiveRecordId,
            'accessed_by_user_id' => $userId,
            'accessed_by_device_id' => $deviceId,
            'reason' => $reason,
            'metadata' => $metadata,
            'accessed_at' => Carbon::now('UTC'),
        ]);
    }
}
