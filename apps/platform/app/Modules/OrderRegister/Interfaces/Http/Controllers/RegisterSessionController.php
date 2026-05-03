<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OrderRegister\Application\Actions\CloseRegisterSession;
use App\Modules\OrderRegister\Application\Actions\OpenRegisterSession;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\OrderRegister\Interfaces\Http\Requests\CloseRegisterSessionRequest;
use App\Modules\OrderRegister\Interfaces\Http\Requests\OpenRegisterSessionRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;

class RegisterSessionController extends Controller
{
    public function open(OpenRegisterSessionRequest $request, OpenRegisterSession $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();
            $registerSession = $action->handle($device, (int) $request->integer('opening_float_minor'));

            return response()->json([
                'data' => [
                    'id' => $registerSession->id,
                    'status' => $registerSession->status,
                    'business_date' => $registerSession->business_date?->format('Y-m-d'),
                    'session_version' => $registerSession->session_version,
                    'opening_float_minor' => $registerSession->opening_float_minor,
                    'expected_cash_minor' => $registerSession->expected_cash_minor,
                ],
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function close(CloseRegisterSessionRequest $request, RegisterSession $registerSession, CloseRegisterSession $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();
            $registerSession = $action->handle(
                $device,
                $registerSession,
                (int) $request->integer('counted_cash_minor'),
                (int) $request->integer('session_version'),
            );

            return response()->json([
                'data' => [
                    'id' => $registerSession->id,
                    'status' => $registerSession->status,
                    'session_version' => $registerSession->session_version,
                    'counted_cash_minor' => $registerSession->counted_cash_minor,
                    'variance_minor' => $registerSession->variance_minor,
                ],
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
