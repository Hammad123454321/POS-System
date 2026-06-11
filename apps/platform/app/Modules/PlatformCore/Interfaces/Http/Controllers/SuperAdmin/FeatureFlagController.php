<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers\SuperAdmin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Application\Features\UpsertGlobalFeatureFlag;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class FeatureFlagController extends Controller
{
    public function index(UpsertGlobalFeatureFlag $action): JsonResponse
    {
        return response()->json(['data' => $action->list()]);
    }

    public function upsert(Request $request, string $flagKey, UpsertGlobalFeatureFlag $action): JsonResponse
    {
        $validated = $request->validate([
            'enabled' => ['required', 'boolean'],
            'value' => ['nullable'],
        ]);

        /** @var User $user */
        $user = $request->user();

        try {
            $result = $action->handle(
                $user,
                $flagKey,
                (bool) $validated['enabled'],
                $validated['value'] ?? null,
            );
        } catch (\InvalidArgumentException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }

        return response()->json(['data' => $result]);
    }
}
