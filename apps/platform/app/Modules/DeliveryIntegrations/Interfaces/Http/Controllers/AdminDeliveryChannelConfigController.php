<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\DeliveryIntegrations\Application\Actions\DispatchMenuPublish;
use App\Modules\DeliveryIntegrations\Application\Actions\PushStoreAvailabilityForChannel;
use App\Modules\DeliveryIntegrations\Application\Actions\UpsertDeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Requests\SetStoreAvailabilityRequest;
use App\Modules\DeliveryIntegrations\Interfaces\Http\Requests\UpsertDeliveryChannelConfigRequest;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminDeliveryChannelConfigController extends Controller
{
    public function store(
        UpsertDeliveryChannelConfigRequest $request,
        Store $store,
        UpsertDeliveryChannelConfig $action,
    ): JsonResponse {
        $config = $action->handle(
            store: $store,
            channelKey: $request->string('channel_key')->toString(),
            isEnabled: $request->boolean('is_enabled'),
            credentials: $request->input('credentials'),
            mapping: $request->input('mapping'),
            pauseWindows: $request->input('pause_windows'),
            defaultPrepTimeMinutes: $request->filled('default_prep_time_minutes') ? (int) $request->integer('default_prep_time_minutes') : null,
            syncHoursEnabled: $request->filled('sync_hours_enabled') ? $request->boolean('sync_hours_enabled') : null,
            syncPrepTimeEnabled: $request->filled('sync_prep_time_enabled') ? $request->boolean('sync_prep_time_enabled') : null,
            syncMenuEnabled: $request->filled('sync_menu_enabled') ? $request->boolean('sync_menu_enabled') : null,
        );

        return response()->json(['data' => $config], 201);
    }

    public function publishMenuNow(
        Request $request,
        Store $store,
        DeliveryChannelConfig $deliveryChannelConfig,
        DispatchMenuPublish $action,
    ): JsonResponse {
        if ($deliveryChannelConfig->merchant_id !== $store->merchant_id || $deliveryChannelConfig->store_id !== $store->id) {
            return response()->json(['message' => 'Delivery channel config does not belong to this store.'], 422);
        }

        $action->handle($store, $deliveryChannelConfig);

        return response()->json([
            'data' => [
                'queued' => true,
                'delivery_channel_config_id' => $deliveryChannelConfig->id,
            ],
        ], 202);
    }

    public function disconnect(Store $store, DeliveryChannelConfig $deliveryChannelConfig): JsonResponse
    {
        if ($deliveryChannelConfig->merchant_id !== $store->merchant_id || $deliveryChannelConfig->store_id !== $store->id) {
            return response()->json(['message' => 'Delivery channel config does not belong to this store.'], 422);
        }

        $deliveryChannelConfig->forceFill([
            'is_enabled' => false,
            'credentials' => null,
        ])->save();

        return response()->json([
            'data' => [
                'id' => $deliveryChannelConfig->id,
                'is_enabled' => false,
            ],
        ]);
    }

    public function setStoreAvailability(
        SetStoreAvailabilityRequest $request,
        Store $store,
        DeliveryChannelConfig $deliveryChannelConfig,
        PushStoreAvailabilityForChannel $action,
    ): JsonResponse {
        try {
            $user = $request->user();
            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may manage delivery availability.');
            }

            return response()->json([
                'data' => $action->handle(
                    actor: $user,
                    store: $store,
                    deliveryChannelConfig: $deliveryChannelConfig,
                    state: $request->string('state')->toString(),
                    reason: $request->filled('reason') ? $request->string('reason')->toString() : null,
                ),
            ]);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
