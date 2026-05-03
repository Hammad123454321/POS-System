<?php

namespace App\Modules\Restaurant\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Restaurant\Application\Actions\CreatePrintRoute;
use App\Modules\Restaurant\Interfaces\Http\Requests\CreatePrintRouteRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminPrintRouteController extends Controller
{
    public function __invoke(
        CreatePrintRouteRequest $request,
        Store $store,
        CreatePrintRoute $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create print routes.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('route_key')->toString(),
                    $request->string('document_type')->toString(),
                    $request->string('primary_printer_config_id')->toString(),
                    $request->filled('secondary_printer_config_id')
                        ? $request->string('secondary_printer_config_id')->toString()
                        : null,
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
