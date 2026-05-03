<?php

namespace App\Modules\PlatformCore\Application\Features;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\FeatureFlag;
use App\Modules\PlatformCore\Domain\Models\FeatureFlagOverride;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class UpsertFeatureFlag
{
    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $flagKey,
        string $scope,
        bool $enabled,
        mixed $value = null,
    ): array {
        if (! $this->canManageSelfServiceFlags($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage feature flags for this store.');
        }

        $definitions = (array) config('pos.feature_flags.definitions', []);
        $definition = $definitions[$flagKey] ?? null;

        if (! is_array($definition)) {
            throw new AuthorizationException('This feature flag is not registered for self-service rollout.');
        }

        if (! (bool) ($definition['self_service'] ?? false)) {
            throw new AuthorizationException('This feature flag may only be managed by Super Admin.');
        }

        $encodedValue = $value === null ? null : json_encode($value, JSON_THROW_ON_ERROR);

        if ($scope === 'merchant') {
            $flag = FeatureFlag::query()->updateOrCreate(
                [
                    'merchant_id' => $store->merchant_id,
                    'flag_key' => $flagKey,
                ],
                [
                    'is_enabled' => $enabled,
                    'value_json' => $encodedValue,
                    'is_self_service' => true,
                    'updated_by_user_id' => $actor->id,
                ],
            );

            return $this->snapshot('merchant', $flag->flag_key, $flag->is_enabled, $value, $store->id);
        }

        $override = FeatureFlagOverride::query()->updateOrCreate(
            [
                'merchant_id' => $store->merchant_id,
                'store_id' => $store->id,
                'flag_key' => $flagKey,
            ],
            [
                'is_enabled' => $enabled,
                'value_json' => $encodedValue,
                'updated_by_user_id' => $actor->id,
            ],
        );

        return $this->snapshot('store', $override->flag_key, $override->is_enabled, $value, $store->id);
    }

    /**
     * @return array<string, mixed>
     */
    private function snapshot(
        string $scope,
        string $flagKey,
        bool $enabled,
        mixed $value,
        string $storeId,
    ): array {
        return [
            'flag_key' => $flagKey,
            'scope' => $scope,
            'store_id' => $scope === 'store' ? $storeId : null,
            'enabled' => $enabled,
            'value' => $enabled ? ($value ?? true) : false,
        ];
    }

    private function canManageSelfServiceFlags(User $actor, Store $store): bool
    {
        return DB::table('user_store_role')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->where('user_store_role.user_id', $actor->id)
            ->where('user_store_role.store_id', $store->id)
            ->where('roles.name', 'Merchant Owner')
            ->exists();
    }
}
