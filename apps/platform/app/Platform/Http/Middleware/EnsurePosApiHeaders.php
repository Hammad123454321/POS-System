<?php

namespace App\Platform\Http\Middleware;

use Closure;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use Symfony\Component\HttpFoundation\Response;

class EnsurePosApiHeaders
{
    public function handle(Request $request, Closure $next): Response
    {
        $requiredHeaders = [
            'X-POS-App-Version',
            'X-Device-Protocol-Version',
            'X-Platform',
        ];

        foreach ($requiredHeaders as $header) {
            if ($request->header($header) === null) {
                return new JsonResponse([
                    'message' => "Missing required header: {$header}.",
                ], Response::HTTP_BAD_REQUEST);
            }
        }

        $requestedMajor = $this->requestedMajor($request);
        $supportedMajors = array_map('intval', config('pos.api.supported_majors', [1]));
        $minSupportedMajor = (int) config('pos.api.min_supported_major', 1);

        if (! in_array($requestedMajor, $supportedMajors, true) || $requestedMajor < $minSupportedMajor) {
            return new JsonResponse([
                'message' => 'This POS API major version is no longer supported.',
                'current_major' => (int) config('pos.api.current_major', 1),
                'supported_majors' => $supportedMajors,
                'min_supported_major' => $minSupportedMajor,
                'sunset_at' => config('pos.api.sunset_at'),
                'upgrade_required' => true,
            ], Response::HTTP_UPGRADE_REQUIRED);
        }

        $minSupportedAppVersion = (string) config('pos.api.min_supported_app_version', '0.1.0');
        $appVersion = (string) $request->header('X-POS-App-Version');

        if (version_compare($appVersion, $minSupportedAppVersion, '<') || $this->isPastSunset()) {
            return new JsonResponse([
                'message' => 'The POS application version is below the minimum supported version.',
                'current_major' => (int) config('pos.api.current_major', 1),
                'supported_majors' => $supportedMajors,
                'min_supported_major' => $minSupportedMajor,
                'min_supported_app_version' => $minSupportedAppVersion,
                'sunset_at' => config('pos.api.sunset_at'),
                'upgrade_required' => true,
            ], Response::HTTP_UPGRADE_REQUIRED);
        }

        $request->attributes->set('pos_api_major', $requestedMajor);

        // Drop the {major} URL parameter from the route now that we've captured it.
        // If we leave it in the route parameters array, Laravel's controller
        // dependency resolver will spill that string into a positional slot
        // (Argument #2) and clobber typehinted model bindings further down the
        // pipeline (e.g. RegisterSession, Appointment) with a "string given" error.
        $request->route()?->forgetParameter('major');

        return $next($request);
    }

    private function requestedMajor(Request $request): int
    {
        $routeMajor = $request->route('major');

        if (is_numeric($routeMajor)) {
            return (int) $routeMajor;
        }

        if (preg_match('#/pos/v(\d+)(?:/|$)#', '/'.$request->path(), $matches) === 1) {
            return (int) $matches[1];
        }

        return (int) config('pos.api.current_major', 1);
    }

    private function isPastSunset(): bool
    {
        $sunsetAt = config('pos.api.sunset_at');

        if ($sunsetAt === null || $sunsetAt === '') {
            return false;
        }

        return Carbon::now('UTC')->greaterThanOrEqualTo(Carbon::parse($sunsetAt, 'UTC'));
    }
}
