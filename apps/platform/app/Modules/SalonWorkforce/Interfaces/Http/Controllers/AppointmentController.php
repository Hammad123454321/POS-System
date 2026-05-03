<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Concurrency\EditLeaseException;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\EditLease;
use App\Modules\SalonWorkforce\Application\Actions\CheckInAppointment;
use App\Modules\SalonWorkforce\Application\Actions\ClaimAppointmentSlot;
use App\Modules\SalonWorkforce\Application\Actions\CompleteAppointment;
use App\Modules\SalonWorkforce\Application\Actions\CreateAppointment;
use App\Modules\SalonWorkforce\Application\Actions\HeartbeatAppointmentSlotClaim;
use App\Modules\SalonWorkforce\Application\Actions\ReleaseAppointmentSlotClaim;
use App\Modules\SalonWorkforce\Application\Queries\ListAppointmentsForDevice;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\ClaimAppointmentSlotRequest;
use App\Modules\SalonWorkforce\Interfaces\Http\Requests\CreateAppointmentRequest;
use DomainException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AppointmentController extends Controller
{
    public function index(Request $request, ListAppointmentsForDevice $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json([
            'data' => $query->handle(
                $device,
                $request->query('start_date'),
                $request->query('end_date'),
            ),
        ]);
    }

    public function claimSlot(
        ClaimAppointmentSlotRequest $request,
        ClaimAppointmentSlot $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    $device,
                    $request->string('staff_profile_id')->toString(),
                    $request->string('starts_at')->toString(),
                    $request->string('ends_at')->toString(),
                ),
            ]);
        } catch (EditLeaseException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
                'lease_version' => $exception->leaseVersion,
                'current_holder_device_id' => $exception->currentHolderDeviceId,
                'lease_expired_at' => $exception->leaseExpiredAt,
                'lease_expires_at' => $exception->leaseExpiresAt,
            ], $exception->status);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function heartbeatSlot(
        Request $request,
        EditLease $slotClaim,
        HeartbeatAppointmentSlotClaim $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle($device, $slotClaim),
            ]);
        } catch (EditLeaseException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
                'lease_version' => $exception->leaseVersion,
                'current_holder_device_id' => $exception->currentHolderDeviceId,
                'lease_expired_at' => $exception->leaseExpiredAt,
                'lease_expires_at' => $exception->leaseExpiresAt,
            ], $exception->status);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function releaseSlot(
        Request $request,
        EditLease $slotClaim,
        ReleaseAppointmentSlotClaim $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle($device, $slotClaim),
            ]);
        } catch (EditLeaseException $exception) {
            return response()->json([
                'message' => $exception->getMessage(),
                'error_code' => $exception->errorCode,
                'lease_version' => $exception->leaseVersion,
                'current_holder_device_id' => $exception->currentHolderDeviceId,
                'lease_expired_at' => $exception->leaseExpiredAt,
                'lease_expires_at' => $exception->leaseExpiresAt,
            ], $exception->status);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function store(
        CreateAppointmentRequest $request,
        CreateAppointment $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    $device,
                    $request->string('slot_claim_id')->toString(),
                    $request->string('staff_profile_id')->toString(),
                    $request->string('service_item_id')->toString(),
                    $request->filled('customer_id') ? $request->string('customer_id')->toString() : null,
                    $request->string('starts_at')->toString(),
                    $request->string('ends_at')->toString(),
                    $request->string('source', 'walk_in')->toString(),
                    $request->filled('discount_minor') ? (int) $request->integer('discount_minor') : 0,
                    $request->filled('notes') ? $request->string('notes')->toString() : null,
                ),
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function checkIn(
        Request $request,
        Appointment $appointment,
        CheckInAppointment $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle($device, $appointment),
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function complete(
        Request $request,
        Appointment $appointment,
        CompleteAppointment $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle($device, $appointment),
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
