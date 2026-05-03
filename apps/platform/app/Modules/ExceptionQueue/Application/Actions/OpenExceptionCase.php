<?php

namespace App\Modules\ExceptionQueue\Application\Actions;

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;

class OpenExceptionCase
{
    /**
     * @param  array<string, mixed>|null  $details
     */
    public function handle(
        ?string $merchantId,
        ?string $storeId,
        string $module,
        string $code,
        string $severity,
        string $title,
        ?array $details = null,
        ?string $relatedType = null,
        ?string $relatedId = null,
        ?string $openedByDeviceId = null,
        ?int $openedByUserId = null,
    ): ExceptionCase {
        return ExceptionCase::query()->create([
            'merchant_id' => $merchantId,
            'store_id' => $storeId,
            'module' => $module,
            'code' => $code,
            'severity' => $severity,
            'title' => $title,
            'details' => $details,
            'related_type' => $relatedType,
            'related_id' => $relatedId,
            'opened_by_device_id' => $openedByDeviceId,
            'opened_by_user_id' => $openedByUserId,
        ]);
    }
}
