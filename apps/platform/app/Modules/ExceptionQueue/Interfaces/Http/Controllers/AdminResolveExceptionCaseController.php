<?php

namespace App\Modules\ExceptionQueue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\ExceptionQueue\Application\Actions\ResolveExceptionCase;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\ExceptionQueue\Interfaces\Http\Requests\ResolveExceptionCaseRequest;
use DomainException;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminResolveExceptionCaseController extends Controller
{
    public function __invoke(
        ResolveExceptionCaseRequest $request,
        ExceptionCase $exceptionCase,
        ResolveExceptionCase $action,
    ): JsonResponse {
        try {
            $user = $request->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may resolve exception cases.');
            }

            return response()->json([
                'data' => $action->handle(
                    $user,
                    $exceptionCase,
                    $request->string('resolution_code')->toString(),
                    $request->filled('notes') ? $request->string('notes')->toString() : null,
                ),
            ]);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
