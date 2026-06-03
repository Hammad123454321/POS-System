<?php

namespace App\Modules\CustomerValue\Application\Privacy;

use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\StoredValue\Domain\Models\GiftCard;
use App\Modules\StoredValue\Domain\Models\MembershipPlan;
use App\Modules\CustomerValue\Domain\Models\MemberAccount;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;

class ExportCustomerData
{
    /**
     * @return array<string, mixed>
     */
    public function handle(Customer $customer, int $actorId): array
    {
        $customer->loadMissing('memberAccount');
        $payload = [
            'exported_at' => Carbon::now('UTC')->toIso8601String(),
            'actor_user_id' => $actorId,
            'customer' => $customer->only([
                'id',
                'merchant_id',
                'name',
                'phone',
                'email',
                'is_active',
                'created_at',
                'updated_at',
                'pii_tombstoned_at',
            ]),
            'member_account' => $customer->memberAccount?->only([
                'id',
                'member_number',
                'status',
                'created_at',
                'updated_at',
            ]),
            'orders' => Order::query()
                ->where('merchant_id', $customer->merchant_id)
                ->where('customer_id', $customer->id)
                ->latest('created_at')
                ->limit(500)
                ->get(['id', 'store_id', 'order_number', 'status', 'total_minor', 'currency', 'business_date', 'created_at']),
            'gift_cards' => GiftCard::query()
                ->where('merchant_id', $customer->merchant_id)
                ->where('issued_to_customer_id', $customer->id)
                ->get(['id', 'code', 'status', 'current_balance_minor', 'currency', 'created_at']),
            'membership_accounts' => MemberAccount::query()
                ->where('merchant_id', $customer->merchant_id)
                ->where('customer_id', $customer->id)
                ->get(['id', 'member_number', 'status', 'created_at']),
            'membership_plans' => MembershipPlan::query()
                ->where('merchant_id', $customer->merchant_id)
                ->get(['id', 'code', 'name', 'price_minor', 'currency', 'duration_days']),
        ];

        $path = sprintf(
            'privacy-exports/%s/%s-%s.json',
            $customer->merchant_id,
            $customer->id,
            Str::lower(Str::random(10)),
        );

        Storage::disk('local')->put($path, json_encode($payload, JSON_PRETTY_PRINT | JSON_THROW_ON_ERROR));

        return [
            'path' => $path,
            'record_count' => [
                'orders' => count($payload['orders']),
                'gift_cards' => count($payload['gift_cards']),
                'membership_accounts' => count($payload['membership_accounts']),
            ],
        ];
    }
}
