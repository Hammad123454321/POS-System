<?php

namespace App\Modules\Restaurant\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Restaurant\Application\Actions\CreatePrinterConfig;
use App\Modules\Restaurant\Interfaces\Http\Requests\CreatePrinterConfigRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminPrinterConfigController extends Controller
{
    public function __invoke(
        CreatePrinterConfigRequest $request,
        Store $store,
        CreatePrinterConfig $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create printer configs.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('name')->toString(),
                    $request->string('driver_key')->toString(),
                    $request->input('connection_config'),
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
