<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Application\DeviceAuth\CreateEnrollmentCode;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\PlatformCore\Interfaces\Http\Requests\CreateEnrollmentCodeRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class DeviceEnrollmentCodeController extends Controller
{
    public function __invoke(
        CreateEnrollmentCodeRequest $request,
        Store $store,
        CreateEnrollmentCode $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create enrollment codes.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('device_profile_id')->toString(),
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
