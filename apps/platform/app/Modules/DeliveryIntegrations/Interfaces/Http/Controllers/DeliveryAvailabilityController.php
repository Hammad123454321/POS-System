<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\DeliveryIntegrations\Application\Actions\PropagateItemAvailability;
use App\Modules\DeliveryIntegrations\Application\Actions\PropagateStoreAvailability;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Requests\SetItemAvailabilityRequest;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Requests\SetStoreAvailabilityRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;

class DeliveryAvailabilityController extends Controller
{
    public function setStoreAvailability(
        SetStoreAvailabilityRequest $request,
        PropagateStoreAvailability $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();

            $channels = $action->handle(
                device: $device,
                state: $request->string('state')->toString(),
                reason: $request->filled('reason') ? $request->string('reason')->toString() : null,
                channelKey: $request->filled('channel_key') ? $request->string('channel_key')->toString() : null,
            );

            return response()->json([
                'data' => [
                    'state' => $request->string('state')->toString(),
                    'channels' => $channels,
                ],
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }

    public function setItemAvailability(
        SetItemAvailabilityRequest $request,
        CatalogItem $catalogItem,
        PropagateItemAvailability $action,
    ): JsonResponse {
        try {
            /** @var Device $device */
            $device = $request->user();
            $soldOut = $request->boolean('sold_out');

            $action->handle($device, $catalogItem, $soldOut);

            return response()->json([
                'data' => [
                    'catalog_item_id' => $catalogItem->id,
                    'sold_out' => $soldOut,
                ],
            ]);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
