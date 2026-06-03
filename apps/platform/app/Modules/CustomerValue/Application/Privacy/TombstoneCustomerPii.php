<?php

namespace App\Modules\CustomerValue\Application\Privacy;

use App\Modules\CustomerValue\Domain\Models\Customer;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;

class TombstoneCustomerPii
{
    /**
     * @return array<string, mixed>
     */
    public function handle(Customer $customer, int $actorId, string $reason): array
    {
        return DB::transaction(function () use ($customer, $actorId, $reason): array {
            $customer->forceFill([
                'name' => 'Deleted Customer '.$customer->id,
                'phone' => null,
                'email' => null,
                'is_active' => false,
                'pii_tombstoned_at' => Carbon::now('UTC'),
                'pii_tombstone_reason' => $reason,
                'pii_tombstoned_by_user_id' => $actorId,
            ])->save();

            $customer->memberAccount()->update([
                'status' => 'tombstoned',
            ]);

            return [
                'customer_id' => $customer->id,
                'pii_tombstoned_at' => $customer->pii_tombstoned_at?->toIso8601String(),
                'financial_records_preserved' => true,
            ];
        });
    }
}
