<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Application\Features\UpsertFeatureFlag;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\PlatformCore\Interfaces\Http\Requests\UpsertFeatureFlagRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;
use JsonException;

class StoreFeatureFlagController extends Controller
{
    public function __invoke(
        UpsertFeatureFlagRequest $request,
        Store $store,
        string $flagKey,
        UpsertFeatureFlag $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may manage feature flags.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $flagKey,
                    $request->string('scope')->toString(),
                    (bool) $request->boolean('enabled'),
                    $request->input('value'),
                ),
            ]);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        } catch (JsonException $exception) {
            return response()->json(['message' => 'Feature flag value must be valid JSON-serializable data.'], 422);
        }
    }
}
