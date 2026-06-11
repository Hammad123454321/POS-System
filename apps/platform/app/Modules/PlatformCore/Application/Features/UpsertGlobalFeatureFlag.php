<?php

namespace App\Modules\PlatformCore\Application\Features;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\FeatureFlag;
use Illuminate\Support\Facades\DB;

/**
 * Super-admin management of platform-wide (merchant_id NULL) feature flags.
 * Unlike the merchant self-service path, Super Admin may toggle ANY registered
 * flag, regardless of its self_service designation.
 */
class UpsertGlobalFeatureFlag
{
    /**
     * @return array<string, mixed>
     */
    public function handle(User $actor, string $flagKey, bool $enabled, mixed $value = null): array
    {
        $definitions = (array) config('pos.feature_flags.definitions', []);

        if (! array_key_exists($flagKey, $definitions)) {
            throw new \InvalidArgumentException('Unknown feature flag key.');
        }

        $encodedValue = $value === null ? null : json_encode($value, JSON_THROW_ON_ERROR);

        // Global rows are identified by a NULL merchant_id. updateOrCreate with a
        // NULL key column is unreliable across drivers, so resolve manually.
        $existingId = DB::table('feature_flags')
            ->whereNull('merchant_id')
            ->where('flag_key', $flagKey)
            ->value('id');

        if ($existingId !== null) {
            FeatureFlag::query()->whereKey($existingId)->update([
                'is_enabled' => $enabled,
                'value_json' => $encodedValue,
                'is_self_service' => false,
                'updated_by_user_id' => $actor->id,
            ]);
        } else {
            FeatureFlag::query()->create([
                'merchant_id' => null,
                'flag_key' => $flagKey,
                'is_enabled' => $enabled,
                'value_json' => $encodedValue,
                'is_self_service' => false,
                'updated_by_user_id' => $actor->id,
            ]);
        }

        return [
            'flag_key' => $flagKey,
            'scope' => 'global',
            'enabled' => $enabled,
            'value' => $enabled ? ($value ?? true) : false,
        ];
    }

    /**
     * @return array<int, array<string, mixed>>
     */
    public function list(): array
    {
        $definitions = (array) config('pos.feature_flags.definitions', []);

        $globalRows = FeatureFlag::query()
            ->whereNull('merchant_id')
            ->get()
            ->keyBy('flag_key');

        $out = [];

        foreach ($definitions as $flagKey => $definition) {
            /** @var FeatureFlag|null $row */
            $row = $globalRows->get($flagKey);

            $out[] = [
                'flag_key' => $flagKey,
                'self_service' => (bool) ($definition['self_service'] ?? false),
                'default' => $definition['default'] ?? false,
                'enabled' => $row?->is_enabled ?? (bool) ($definition['default'] ?? false),
                'is_overridden_globally' => $row !== null,
            ];
        }

        return $out;
    }
}
