<?php

namespace App\Modules\Audit\Application;

use App\Modules\Audit\Domain\Models\AuditLog;
use Carbon\CarbonImmutable;

class AuditLogger
{
    /**
     * @param  array<string, mixed>|null  $beforeState
     * @param  array<string, mixed>|null  $afterState
     * @param  array<string, mixed>|null  $metadata
     */
    public function log(
        ?string $merchantId,
        ?string $storeId,
        string $module,
        string $action,
        ?string $subjectType = null,
        ?string $subjectId = null,
        ?array $beforeState = null,
        ?array $afterState = null,
        ?array $metadata = null,
        ?int $userId = null,
        ?string $deviceId = null,
    ): AuditLog {
        return AuditLog::query()->create([
            'merchant_id' => $merchantId,
            'store_id' => $storeId,
            'user_id' => $userId,
            'device_id' => $deviceId,
            'module' => $module,
            'action' => $action,
            'subject_type' => $subjectType,
            'subject_id' => $subjectId,
            'before_state' => $beforeState,
            'after_state' => $afterState,
            'metadata' => $metadata,
            'occurred_at' => CarbonImmutable::now('UTC'),
        ]);
    }
}
