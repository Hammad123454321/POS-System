<?php

namespace App\Modules\CustomerValue\Application\Queries;

use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\PlatformCore\Domain\Models\Device;

class SearchCustomersForDevice
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function handle(Device $device, ?string $search): array
    {
        $term = trim((string) $search);

        $customers = Customer::query()
            ->forMerchant($device->merchant_id)
            ->where('is_active', true)
            ->with('memberAccount')
            ->when($term !== '', function ($query) use ($term): void {
                $query->where(function ($inner) use ($term): void {
                    $inner->where('name', 'like', "%{$term}%")
                        ->orWhere('phone', 'like', "%{$term}%")
                        ->orWhere('email', 'like', "%{$term}%")
                        ->orWhereHas('memberAccount', function ($memberQuery) use ($term): void {
                            $memberQuery->where('member_number', 'like', "%{$term}%");
                        });
                });
            })
            ->orderBy('name')
            ->limit(20)
            ->get();

        return $customers->map(function (Customer $customer): array {
            $memberAccount = $customer->memberAccount;

            return [
                'id' => $customer->id,
                'name' => $customer->name,
                'phone' => $customer->phone,
                'email' => $customer->email,
                'member_account' => $memberAccount === null ? null : [
                    'id' => $memberAccount->id,
                    'member_number' => $memberAccount->member_number,
                    'status' => $memberAccount->status,
                ],
            ];
        })->values()->all();
    }
}
