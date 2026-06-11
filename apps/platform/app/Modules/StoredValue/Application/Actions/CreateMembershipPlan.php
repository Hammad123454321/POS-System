<?php

namespace App\Modules\StoredValue\Application\Actions;

use App\Models\User;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\StoredValue\Domain\Models\MembershipPlan;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class CreateMembershipPlan
{
    /**
     * @param  array<string, mixed>|null  $benefitsSnapshot
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        Store $store,
        string $name,
        ?string $code,
        int $priceMinor,
        int $durationDays,
        ?array $benefitsSnapshot = null,
    ): array {
        if (! $this->canManageStoreConfig($actor, $store)) {
            throw new AuthorizationException('You are not allowed to manage membership plans for this store.');
        }

        $plan = MembershipPlan::query()->create([
            'merchant_id' => $store->merchant_id,
            'name' => $name,
            'code' => $code,
            'price_minor' => $priceMinor,
            'currency' => $store->merchant()->firstOrFail()->currency,
            'duration_days' => $durationDays,
            'benefits_snapshot' => $benefitsSnapshot,
            'is_active' => true,
        ]);

        return [
            'id' => $plan->id,
            'name' => $plan->name,
            'code' => $plan->code,
            'price_minor' => $plan->price_minor,
            'currency' => $plan->currency,
            'duration_days' => $plan->duration_days,
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
