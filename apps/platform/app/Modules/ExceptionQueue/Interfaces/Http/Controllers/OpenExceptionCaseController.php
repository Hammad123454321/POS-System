<?php

namespace App\Modules\ExceptionQueue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\ExceptionQueue\Application\Queries\ListOpenStoreExceptions;
use App\Modules\PlatformCore\Domain\Models\Device;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class OpenExceptionCaseController extends Controller
{
    public function __invoke(Request $request, ListOpenStoreExceptions $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json($query->handle($device));
    }
}
