<?php

namespace App\Modules\PlatformCore\Application\Features;

use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\PlatformCore\Domain\Models\FeatureFlag;
use App\Modules\PlatformCore\Domain\Models\FeatureFlagOverride;

class FeatureFlagSnapshotQuery
{
    /**
     * @return array<string, mixed>
     */
    public function forDevice(Device $device): array
    {
        $definitions = (array) config('pos.feature_flags.definitions', []);

        if ($definitions === []) {
            return [];
        }

        $globalFlags = FeatureFlag::query()
            ->whereNull('merchant_id')
            ->get()
            ->keyBy('flag_key');

        $merchantFlags = FeatureFlag::query()
            ->where('merchant_id', $device->merchant_id)
            ->get()
            ->keyBy('flag_key');

        $storeOverrides = FeatureFlagOverride::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->get()
            ->keyBy('flag_key');

        $snapshot = [];

        foreach ($definitions as $flagKey => $definition) {
            $value = $definition['default'] ?? false;

            // Platform-wide override (merchant_id NULL) sits beneath merchant flags.
            /** @var FeatureFlag|null $globalFlag */
            $globalFlag = $globalFlags->get($flagKey);

            if ($globalFlag !== null) {
                $value = $globalFlag->is_enabled
                    ? $this->decodeValue($globalFlag->value_json, true)
                    : false;
            }

            /** @var FeatureFlag|null $merchantFlag */
            $merchantFlag = $merchantFlags->get($flagKey);

            if ($merchantFlag !== null) {
                $value = $merchantFlag->is_enabled
                    ? $this->decodeValue($merchantFlag->value_json, true)
                    : false;
            }

            /** @var FeatureFlagOverride|null $storeOverride */
            $storeOverride = $storeOverrides->get($flagKey);

            if ($storeOverride !== null) {
                $value = $storeOverride->is_enabled
                    ? $this->decodeValue($storeOverride->value_json, true)
                    : false;
            }

            $snapshot[$flagKey] = $value;
        }

        return $snapshot;
    }

    private function decodeValue(?string $encoded, mixed $fallback): mixed
    {
        if ($encoded === null || $encoded === '') {
            return $fallback;
        }

        $decoded = json_decode($encoded, true);

        return json_last_error() === JSON_ERROR_NONE ? $decoded : $fallback;
    }
}
