<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\DeliveryIntegrations\Application\Actions\CancelExternalOrder;
use App\Modules\DeliveryIntegrations\Application\Actions\ConfirmExternalOrder;
use App\Modules\DeliveryIntegrations\Application\Actions\IngestExternalDeliveryOrder;
use App\Modules\DeliveryIntegrations\Application\Actions\UpdateExternalOrderStatus;
use App\Modules\DeliveryIntegrations\Application\Queries\ListPendingExternalOrdersForDevice;
use App\Modules\DeliveryIntegrations\Domain\Models\ExternalOrderLink;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Requests\CancelDeliveryOrderRequest;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Requests\IngestExternalDeliveryOrderRequest;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Requests\UpdateDeliveryOrderStatusRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DeliveryOrderController extends Controller
{
    public function index(Request $request, ListPendingExternalOrdersForDevice $query): JsonResponse
    {
        /** @var Device $device */
        $device = $request->user();

        return response()->json([
            'data' => $query->handle($device),
        ]);
    }

    public function ingest(
        IngestExternalDeliveryOrderRequest $request,
        IngestExternalDeliveryOrder $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();
            $payload = (array) ($request->input('payload') ?? []);
            $payload['external_order_id'] = $request->string('external_order_id')->toString();
            if ($request->filled('external_store_id')) {
                $payload['external_store_id'] = $request->string('external_store_id')->toString();
            }
            if ($request->filled('lines')) {
                $payload['lines'] = $request->input('lines');
            }

            $result = $action->handle(
                device: $device,
                channelKey: $request->string('channel_key')->toString(),
                payload: $payload,
            );

            return response()->json(['data' => $result], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function confirm(
        Request $request,
        ExternalOrderLink $externalOrderLink,
        ConfirmExternalOrder $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle($device, $externalOrderLink),
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function updateStatus(
        UpdateDeliveryOrderStatusRequest $request,
        ExternalOrderLink $externalOrderLink,
        UpdateExternalOrderStatus $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle($device, $externalOrderLink, $request->string('status')->toString()),
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function cancel(
        CancelDeliveryOrderRequest $request,
        ExternalOrderLink $externalOrderLink,
        CancelExternalOrder $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    device: $device,
                    externalOrderLink: $externalOrderLink,
                    reason: $request->filled('reason') ? $request->string('reason')->toString() : null,
                ),
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
