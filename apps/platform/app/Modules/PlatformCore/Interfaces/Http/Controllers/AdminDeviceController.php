<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class AdminDeviceController extends Controller
{
    public function index(Request $request, Store $store): JsonResponse
    {
        Gate::authorize('manageDevices', $store);

        $devices = $store->devices()
            ->when($request->query('status'), fn ($q, string $status) => $q->where('status', $status))
            ->with('profile:id,name,type')
            ->orderByDesc('created_at')
            ->get(['id', 'name', 'platform', 'status', 'device_profile_id', 'drawer_code', 'last_seen_at']);

        return response()->json(['data' => $devices]);
    }

    public function deactivate(Store $store, Device $device): JsonResponse
    {
        Gate::authorize('manageDevices', $store);

        abort_unless($device->store_id === $store->id, 404);

        $device->forceFill(['status' => 'disabled'])->save();

        return response()->json(['data' => $device->only(['id', 'name', 'status'])]);
    }
}
