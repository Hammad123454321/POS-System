<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers\SuperAdmin;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Onboarding\CreateDeviceProfile;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DeviceProfileController extends Controller
{
    public function store(Request $request, CreateDeviceProfile $action): JsonResponse
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:120'],
            'type' => ['required', 'string', 'max:60'],
            'capabilities' => ['nullable', 'array'],
        ]);

        $profile = $action->handle(
            $validated['name'],
            $validated['type'],
            $validated['capabilities'] ?? [],
        );

        return response()->json(['data' => $profile->only(['id', 'name', 'type', 'capabilities'])], 201);
    }
}
