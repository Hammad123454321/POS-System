<?php

namespace App\Platform\Http\Middleware;

use Closure;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
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

        $requestedMajor = 1;
        $supportedMajors = config('pos.api.supported_majors', [1]);
        $minSupportedMajor = config('pos.api.min_supported_major', 1);

        if (! in_array($requestedMajor, $supportedMajors, true) || $requestedMajor < $minSupportedMajor) {
            return new JsonResponse([
                'message' => 'This POS API major version is no longer supported.',
                'min_supported_major' => $minSupportedMajor,
                'upgrade_required' => true,
            ], Response::HTTP_UPGRADE_REQUIRED);
        }

        $minSupportedAppVersion = (string) config('pos.api.min_supported_app_version', '0.1.0');
        $appVersion = (string) $request->header('X-POS-App-Version');

        if (version_compare($appVersion, $minSupportedAppVersion, '<')) {
            return new JsonResponse([
                'message' => 'The POS application version is below the minimum supported version.',
                'min_supported_app_version' => $minSupportedAppVersion,
                'upgrade_required' => true,
            ], Response::HTTP_UPGRADE_REQUIRED);
        }

        $request->attributes->set('pos_api_major', $requestedMajor);

        return $next($request);
    }
}
