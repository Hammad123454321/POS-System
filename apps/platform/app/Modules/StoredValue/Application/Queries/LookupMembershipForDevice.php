<?php

namespace App\Modules\StoredValue\Application\Queries;

use App\Modules\CustomerValue\Domain\Models\MemberAccount;
use App\Modules\PlatformCore\Domain\Models\Device;
use Illuminate\Database\Eloquent\Builder;

class LookupMembershipForDevice
{
    /**
     * @return array<string, mixed>|null
     */
    public function handle(Device $device, ?string $memberNumber = null, ?string $customerId = null): ?array
    {
        /** @var MemberAccount|null $memberAccount */
        $memberAccount = MemberAccount::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('status', 'active')
            ->where(function (Builder $query) use ($customerId, $memberNumber): void {
                if ($customerId !== null && $customerId !== '') {
                    $query->where('customer_id', $customerId);
                }

                if ($memberNumber !== null && $memberNumber !== '') {
                    $query->orWhere('member_number', strtoupper($memberNumber));
                }
            })
            ->with(['customer', 'membershipPlan'])
            ->first();

        if ($memberAccount === null) {
            return null;
        }

        return [
            'member_account_id' => $memberAccount->id,
            'member_number' => $memberAccount->member_number,
            'status' => $memberAccount->status,
            'valid_until' => $memberAccount->valid_until?->toIso8601String(),
            'customer' => [
                'id' => $memberAccount->customer?->id,
                'name' => $memberAccount->customer?->name,
            ],
            'membership_plan' => $memberAccount->membershipPlan === null ? null : [
                'id' => $memberAccount->membershipPlan->id,
                'name' => $memberAccount->membershipPlan->name,
                'code' => $memberAccount->membershipPlan->code,
                'price_minor' => $memberAccount->membershipPlan->price_minor,
                'currency' => $memberAccount->membershipPlan->currency,
                'duration_days' => $memberAccount->membershipPlan->duration_days,
            ],
        ];
    }
}
