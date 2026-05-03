<?php

namespace App\Modules\ExceptionQueue\Application\Queries;

use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;

class ListOpenStoreExceptions
{
    /**
     * @return array<string, mixed>
     */
    public function handle(Device $device): array
    {
        $cases = ExceptionCase::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('status', 'open')
            ->latest('created_at')
            ->limit(10)
            ->get();

        return [
            'count' => $cases->count(),
            'cases' => $cases->map(fn (ExceptionCase $case): array => [
                'id' => $case->id,
                'module' => $case->module,
                'code' => $case->code,
                'severity' => $case->severity,
                'status' => $case->status,
                'title' => $case->title,
                'details' => $case->details ?? [],
                'created_at' => $case->created_at?->toIso8601String(),
            ])->values()->all(),
        ];
    }
}
