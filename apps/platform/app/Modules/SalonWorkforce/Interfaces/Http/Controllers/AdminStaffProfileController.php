<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Actions\CreateStaffProfile;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\CreateStaffProfileRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminStaffProfileController extends Controller
{
    public function __invoke(
        CreateStaffProfileRequest $request,
        Store $store,
        CreateStaffProfile $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create staff profiles.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('display_name')->toString(),
                    $request->filled('code') ? $request->string('code')->toString() : null,
                    $request->filled('role_title') ? $request->string('role_title')->toString() : null,
                    $request->filled('phone') ? $request->string('phone')->toString() : null,
                    $request->filled('email') ? $request->string('email')->toString() : null,
                    $request->filled('hired_on') ? $request->date('hired_on')?->format('Y-m-d') : null,
                    $request->filled('metadata') ? $request->input('metadata') : null,
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
