<?php

namespace App\Modules\CustomerValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\CustomerValue\Application\Actions\CreateCustomer;
use App\Modules\CustomerValue\Interfaces\Http\Requests\CreateCustomerRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminCustomerController extends Controller
{
    public function __invoke(
        CreateCustomerRequest $request,
        Store $store,
        CreateCustomer $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may create customers.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $store,
                    $request->string('name')->toString(),
                    $request->filled('phone') ? $request->string('phone')->toString() : null,
                    $request->filled('email') ? $request->string('email')->toString() : null,
                    $request->filled('member_number') ? $request->string('member_number')->toString() : null,
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
