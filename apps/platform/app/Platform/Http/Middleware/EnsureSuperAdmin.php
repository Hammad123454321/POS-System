<?php

namespace App\Platform\Http\Middleware;

use App\Models\User;
use Closure;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureSuperAdmin
{
    public function handle(Request $request, Closure $next): Response
    {
        $user = $request->user();

        if (! $user instanceof User || ! $user->is_super_admin) {
            return new JsonResponse(['message' => 'Super admin access is required.'], Response::HTTP_FORBIDDEN);
        }

        return $next($request);
    }
}
