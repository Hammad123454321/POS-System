<?php

namespace App\Modules\DeliveryIntegrations\Application\Actions;

use App\Models\User;
use App\Modules\DeliveryIntegrations\Application\Jobs\PushDeliveryUpdateJob;
use App\Modules\DeliveryIntegrations\Domain\Models\DeliveryChannelConfig;
use App\Modules\PlatformCore\Domain\Models\Store;
use Carbon\CarbonImmutable;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class PushStoreAvailabilityForChannel
{
    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        DeliveryChannelConfig $deliveryChannelConfig,
        string $state,
        ?string $reason = null,
    ): array {
        if (! $this->canManageStoreConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage delivery availability for this store.');
        }

        if (
            $deliveryChannelConfig->merchant_id !== $store->merchant_id
            || $deliveryChannelConfig->store_id !== $store->id
            || ! $deliveryChannelConfig->is_enabled
        ) {
            throw new AuthorizationException('Delivery channel config does not belong to this store or is disabled.');
        }

        $resolvedState = $state === 'resume' ? 'open' : $state;
        $propagatedAt = CarbonImmutable::now('UTC')->toIso8601String();

        PushDeliveryUpdateJob::dispatch(
            $deliveryChannelConfig->id,
            'set_store_availability',
            [
                'state' => $resolvedState,
                'reason' => $reason,
                'store_id' => $store->id,
            ],
        )->onConnection((string) config('pos.delivery.queue_connection', config('queue.default')))
            ->onQueue((string) config('pos.delivery.queue', 'delivery'));

        $metadata = $deliveryChannelConfig->metadata ?? [];
        $metadata['store_availability'] = [
            'state' => $resolvedState,
            'reason' => $reason,
            'propagated_at' => $propagatedAt,
            'updated_by_user_id' => $actor->id,
        ];
        $deliveryChannelConfig->forceFill(['metadata' => $metadata])->save();

        return [
            'delivery_channel_config_id' => $deliveryChannelConfig->id,
            'channel_key' => $deliveryChannelConfig->channel_key,
            'state' => $resolvedState,
            'propagated_at' => $propagatedAt,
        ];
    }

    private function canManageStoreConfig(User $actor, Store $store): bool
    {
        if ($actor->is_super_admin) {
            return true;
        }

        return DB::table('user_store_role')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->where('user_store_role.user_id', $actor->id)
            ->where('user_store_role.store_id', $store->id)
            ->whereIn('roles.name', ['Merchant Owner', 'Store Admin'])
            ->exists();
    }
}
