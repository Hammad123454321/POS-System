<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\DeviceAuth\DeviceAuthException;
use App\Modules\PlatformCore\Application\DeviceAuth\EnrollDevice;
use App\Modules\PlatformCore\Application\DeviceAuth\RefreshDeviceToken;
use App\Modules\PlatformCore\Application\DeviceAuth\RevokeDeviceSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Interfaces\Http\Requests\EnrollDeviceRequest;
use App\Modules\PlatformCore\Interfaces\Http\Requests\RefreshDeviceTokenRequest;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class DeviceAuthController extends Controller
{
    public function enroll(EnrollDeviceRequest $request, EnrollDevice $action): JsonResponse
    {
        try {
            return response()->json([
                'data' => $action->handle(
                    $request->string('enrollment_code')->toString(),
                    $request->string('device_name')->toString(),
                    $request->string('platform')->toString(),
                    $request->string('device_fingerprint')->toString(),
                    $request->string('public_key')->toString(),
                    $request->input('attestation'),
                ),
            ], 201);
        } catch (DeviceAuthException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
            ], $exception->status);
        }
    }

    public function refresh(RefreshDeviceTokenRequest $request, RefreshDeviceToken $action): JsonResponse
    {
        try {
            return response()->json([
                'data' => $action->handle($request->string('refresh_token')->toString()),
            ]);
        } catch (DeviceAuthException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
            ], $exception->status);
        }
    }

    public function logout(Request $request, RevokeDeviceSession $action): Response
    {
        /** @var Device $device */
        $device = $request->user();
        $action->handle($request, $device);

        return response()->noContent();
    }
}
