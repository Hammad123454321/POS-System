<?php

namespace App\Modules\StoredValue\Application\Actions;

use App\Modules\Audit\Application\AuditLogger;
use App\Modules\Billing\Application\RecordUsage;
use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\CustomerValue\Domain\Models\MemberAccount;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Domain\Models\MembershipLedgerEntry;
use App\Modules\StoredValue\Domain\Models\MembershipPlan;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class ActivateMembership
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
        private readonly RecordUsage $recordUsage,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        Device $device,
        string $customerId,
        string $membershipPlanId,
        ?string $memberNumber = null,
    ): array {
        return DB::transaction(function () use ($customerId, $device, $memberNumber, $membershipPlanId): array {
            $customer = Customer::query()
                ->where('merchant_id', $device->merchant_id)
                ->whereKey($customerId)
                ->firstOrFail();

            $plan = MembershipPlan::query()
                ->where('merchant_id', $device->merchant_id)
                ->whereKey($membershipPlanId)
                ->where('is_active', true)
                ->firstOrFail();

            /** @var MemberAccount|null $memberAccount */
            $memberAccount = MemberAccount::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('customer_id', $customer->id)
                ->first();

            $startsAt = CarbonImmutable::now('UTC');
            $endsAt = $startsAt->addDays($plan->duration_days);

            if ($memberAccount === null) {
                $memberAccount = MemberAccount::query()->create([
                    'merchant_id' => $device->merchant_id,
                    'customer_id' => $customer->id,
                    'member_number' => $memberNumber === null || $memberNumber === ''
                        ? 'MEM-'.Str::upper(Str::random(8))
                        : Str::upper($memberNumber),
                    'membership_plan_id' => $plan->id,
                    'status' => 'active',
                    'valid_from' => $startsAt,
                    'valid_until' => $endsAt,
                    'benefits_snapshot' => $plan->benefits_snapshot,
                ]);
                $entryType = 'activate';
            } else {
                $memberAccount->forceFill([
                    'membership_plan_id' => $plan->id,
                    'status' => 'active',
                    'valid_from' => $startsAt,
                    'valid_until' => $endsAt,
                    'benefits_snapshot' => $plan->benefits_snapshot,
                ])->save();
                $entryType = 'renew';
            }

            MembershipLedgerEntry::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'member_account_id' => $memberAccount->id,
                'membership_plan_id' => $plan->id,
                'customer_id' => $customer->id,
                'device_id' => $device->id,
                'entry_type' => $entryType,
                'amount_minor' => $plan->price_minor,
                'effective_starts_at' => $startsAt,
                'effective_ends_at' => $endsAt,
                'occurred_at' => $startsAt,
                'metadata' => [
                    'plan_code' => $plan->code,
                ],
            ]);

            $this->auditLogger->log(
                $device->merchant_id,
                $device->store_id,
                'stored_value',
                'membership.'.$entryType.'d',
                'member_account',
                $memberAccount->id,
                null,
                [
                    'member_number' => $memberAccount->member_number,
                    'membership_plan_id' => $plan->id,
                    'valid_until' => $endsAt->toIso8601String(),
                ],
                null,
                null,
                $device->id,
            );
            $this->recordUsage->handle(
                $device->merchant_id,
                $device->store_id,
                'membership.'.$entryType,
            );

            return [
                'member_account_id' => $memberAccount->id,
                'member_number' => $memberAccount->member_number,
                'status' => $memberAccount->status,
                'valid_until' => $memberAccount->valid_until?->toIso8601String(),
                'membership_plan' => [
                    'id' => $plan->id,
                    'name' => $plan->name,
                    'price_minor' => $plan->price_minor,
                    'duration_days' => $plan->duration_days,
                ],
                'customer' => [
                    'id' => $customer->id,
                    'name' => $customer->name,
                ],
            ];
        });
    }
}
