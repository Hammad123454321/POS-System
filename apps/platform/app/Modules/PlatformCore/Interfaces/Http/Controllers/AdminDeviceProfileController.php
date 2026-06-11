<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\DeviceProfile;
use Illuminate\Http\JsonResponse;

/**
 * Device profiles are global (no merchant_id). Back-office users may LIST them
 * to populate the enrollment form; creation is super-admin-only (separate
 * controller in the SuperAdmin namespace).
 */
class AdminDeviceProfileController extends Controller
{
    public function index(): JsonResponse
    {
        $profiles = DeviceProfile::query()
            ->orderBy('name')
            ->get(['id', 'name', 'type', 'capabilities']);

        return response()->json(['data' => $profiles]);
    }
}
