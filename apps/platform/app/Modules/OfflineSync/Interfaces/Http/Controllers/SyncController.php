<?php

namespace App\Modules\OfflineSync\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OfflineSync\Application\Actions\PushSyncEvents;
use App\Modules\OfflineSync\Application\Queries\PullSyncDeltas;
use App\Modules\OfflineSync\Interfaces\Http\Requests\PushSyncEventsRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class SyncController extends Controller
{
    public function push(PushSyncEventsRequest $request, PushSyncEvents $action): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        $accepted = $action->handle(
            $device,
            $request->collect('events')
                ->map(fn (array $event): array => [
                    'local_event_id' => (string) $event['local_event_id'],
                    'entity_type' => (string) $event['entity_type'],
                    'entity_id' => $event['entity_id'] ?? null,
                    'action' => (string) $event['action'],
                    'payload' => $event['payload'],
                ])->values()->all(),
        );

        return response()->json([
            'data' => $accepted,
        ], 202);
    }

    public function pull(Request $request, PullSyncDeltas $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json(
            $query->handle($device, $request->query('cursor')),
        );
    }
}
