<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Application\Actions\CloseShift;
use App\Modules\SalonWorkforce\Application\Actions\OpenShift;
use App\Modules\SalonWorkforce\Domain\Models\Shift;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\CloseShiftRequest;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\OpenShiftRequest;
use DomainException;
use Illuminate\Http\JsonResponse;

class ShiftController extends Controller
{
    public function open(
        OpenShiftRequest $request,
        OpenShift $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    $device,
                    $request->string('staff_profile_id')->toString(),
                    $request->filled('scheduled_start_at') ? $request->string('scheduled_start_at')->toString() : null,
                    $request->filled('scheduled_end_at') ? $request->string('scheduled_end_at')->toString() : null,
                ),
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function close(
        CloseShiftRequest $request,
        Shift $shift,
        CloseShift $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    $device,
                    $shift,
                    (int) $request->integer('session_version'),
                    $request->filled('break_minutes') ? (int) $request->integer('break_minutes') : 0,
                ),
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
