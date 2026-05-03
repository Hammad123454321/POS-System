<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Application\Actions\ApproveAttendanceRecord;
use App\Modules\SalonWorkforce\Domain\Models\AttendanceRecord;
use DomainException;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;

class AdminApproveAttendanceRecordController extends Controller
{
    public function __invoke(
        Store $store,
        AttendanceRecord $attendanceRecord,
        ApproveAttendanceRecord $action,
    ): JsonResponse {
        try {
            $user = request()->user();

            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may approve attendance.');
            }

            return response()->json([
                'data' => $action->handle($user, $store, $attendanceRecord),
            ]);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
