<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Actions\CreateDeviceStatusEvent;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Interfaces\Http\Requests\CreateDeviceStatusEventRequest;
use Illuminate\Http\JsonResponse;

class DeviceStatusEventController extends Controller
{
    public function store(CreateDeviceStatusEventRequest $request, CreateDeviceStatusEvent $action): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        $event = $action->handle(
            device: $device,
            status: $request->string('status')->toString(),
            metadata: $request->input('metadata'),
            occurredAt: $request->filled('occurred_at') ? $request->string('occurred_at')->toString() : null,
        );

        return response()->json(['data' => $event], 201);
    }
}
