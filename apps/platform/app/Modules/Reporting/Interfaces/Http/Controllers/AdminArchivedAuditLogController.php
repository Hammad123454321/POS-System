<?php

namespace App\Modules\Reporting\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Audit\Domain\Models\AuditLog;
use App\Modules\PlatformCore\Application\Archive\ArchiveAccessLogger;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminArchivedAuditLogController extends Controller
{
    public function __invoke(
        Request $request,
        Store $store,
        AuditLog $auditLog,
        ArchiveAccessLogger $archiveAccessLogger,
    ): JsonResponse {
        abort_unless(
            $auditLog->merchant_id === $store->merchant_id
            && ($auditLog->store_id === null || $auditLog->store_id === $store->id),
            404,
        );

        $archiveAccessLogger->log(
            merchantId: $auditLog->merchant_id,
            storeId: $auditLog->store_id ?? $store->id,
            archiveType: 'audit_log',
            archiveRecordId: $auditLog->id,
            userId: (int) $request->user()->getAuthIdentifier(),
            deviceId: null,
            reason: $request->query('reason', 'back-office audit archive read'),
            metadata: [
                'module' => $auditLog->module,
                'action' => $auditLog->action,
                'subject_type' => $auditLog->subject_type,
                'subject_id' => $auditLog->subject_id,
            ],
        );

        return response()->json([
            'data' => [
                'id' => $auditLog->id,
                'module' => $auditLog->module,
                'action' => $auditLog->action,
                'subject_type' => $auditLog->subject_type,
                'subject_id' => $auditLog->subject_id,
                'before_state' => $auditLog->before_state,
                'after_state' => $auditLog->after_state,
                'metadata' => $auditLog->metadata,
                'occurred_at' => $auditLog->occurred_at?->toIso8601String(),
            ],
        ]);
    }
}
