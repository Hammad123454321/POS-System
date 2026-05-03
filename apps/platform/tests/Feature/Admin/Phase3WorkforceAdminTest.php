<?php

use App\Models\User;
use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Identity\Domain\Models\Role;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use App\Modules\SalonWorkforce\Domain\Models\AttendanceRecord;
use App\Modules\SalonWorkforce\Domain\Models\CommissionRule;
use App\Modules\SalonWorkforce\Domain\Models\ServiceItem;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use App\Modules\SalonWorkforce\Domain\Models\StaffServiceRule;
use App\Modules\SalonWorkforce\Domain\Models\WageRule;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\Sanctum;

function buildPhase3AdminContext(): array
{
    $user = User::factory()->create();

    $merchant = Merchant::query()->create([
        'name' => 'Salon Merchant',
        'currency' => 'USD',
        'status' => 'active',
    ]);

    $store = Store::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Salon Store',
        'code' => 'SALON-1',
        'mode' => 'salon',
        'timezone' => 'America/New_York',
        'business_day_cutoff' => '04:00',
        'status' => 'active',
    ]);

    $role = Role::query()->create([
        'merchant_id' => $merchant->id,
        'name' => 'Store Admin',
        'scope' => 'store',
    ]);

    DB::table('user_store_role')->insert([
        'user_id' => $user->id,
        'store_id' => $store->id,
        'role_id' => $role->id,
        'created_at' => now(),
        'updated_at' => now(),
    ]);

    Sanctum::actingAs($user);

    return [$user, $merchant, $store];
}

it('creates phase 3 workforce configuration resources through admin endpoints', function () {
    [, , $store] = buildPhase3AdminContext();

    $staffProfile = $this->postJson("/api/admin/v1/stores/{$store->id}/workforce/staff-profiles", [
        'display_name' => 'Nadia Stylist',
        'code' => 'STAFF-NADIA',
        'role_title' => 'Senior Stylist',
        'hired_on' => '2026-04-01',
    ])->assertCreated()
        ->json('data');

    $serviceItem = $this->postJson("/api/admin/v1/stores/{$store->id}/workforce/service-items", [
        'name' => 'Haircut',
        'sku' => 'SRV-HAIRCUT',
        'base_price_minor' => 4500,
        'duration_minutes' => 60,
        'buffer_minutes' => 10,
        'is_walk_in_enabled' => true,
    ])->assertCreated()
        ->assertJsonPath('data.name', 'Haircut')
        ->json('data');

    $commissionRule = $this->postJson("/api/admin/v1/stores/{$store->id}/workforce/commission-rules", [
        'name' => 'Dynamic Net 15%',
        'base_type' => 'service_net',
        'rate_basis_points' => 1500,
        'currency' => 'USD',
    ])->assertCreated()
        ->assertJsonPath('data.base_type', 'service_net')
        ->json('data');

    $this->postJson("/api/admin/v1/stores/{$store->id}/workforce/wage-rules", [
        'staff_profile_id' => $staffProfile['id'],
        'name' => 'Weekly Hourly',
        'wage_type' => 'hourly',
        'hourly_rate_minor' => 3000,
        'currency' => 'USD',
        'effective_from' => '2026-04-20',
    ])->assertCreated()
        ->assertJsonPath('data.wage_type', 'hourly')
        ->assertJsonPath('data.hourly_rate_minor', 3000);

    $this->postJson("/api/admin/v1/stores/{$store->id}/workforce/staff-service-rules", [
        'staff_profile_id' => $staffProfile['id'],
        'service_item_id' => $serviceItem['id'],
        'commission_rule_id' => $commissionRule['id'],
        'is_active' => true,
    ])->assertOk()
        ->assertJsonPath('data.staff_profile_id', $staffProfile['id'])
        ->assertJsonPath('data.service_item_id', $serviceItem['id']);
});

it('generates weekly payroll snapshots and labor analytics for phase 3', function () {
    [$user, $merchant, $store] = buildPhase3AdminContext();

    $staff = StaffProfile::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'display_name' => 'Rimsha Barber',
        'code' => 'RIMSHA-1',
        'status' => 'active',
    ]);

    $catalogItem = CatalogItem::query()->create([
        'merchant_id' => $merchant->id,
        'category_id' => null,
        'tax_rule_id' => null,
        'type' => 'service',
        'name' => 'Hair Coloring',
        'sku' => 'SRV-COLOR',
        'base_price_minor' => 6000,
        'currency' => 'USD',
        'is_active' => true,
        'sold_out' => false,
    ]);

    $service = ServiceItem::query()->create([
        'merchant_id' => $merchant->id,
        'catalog_item_id' => $catalogItem->id,
        'duration_minutes' => 90,
        'buffer_minutes' => 15,
        'is_walk_in_enabled' => true,
    ]);

    $commission = CommissionRule::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'name' => 'Dynamic Net 15%',
        'base_type' => 'service_net',
        'rate_basis_points' => 1500,
        'currency' => 'USD',
        'is_active' => true,
    ]);

    StaffServiceRule::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'staff_profile_id' => $staff->id,
        'service_item_id' => $service->id,
        'commission_rule_id' => $commission->id,
        'is_active' => true,
    ]);

    WageRule::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'staff_profile_id' => $staff->id,
        'name' => 'Hourly',
        'wage_type' => 'hourly',
        'hourly_rate_minor' => 3000,
        'currency' => 'USD',
        'effective_from' => '2026-04-20',
        'is_active' => true,
    ]);

    AttendanceRecord::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'staff_profile_id' => $staff->id,
        'check_in_at' => '2026-04-20T09:00:00Z',
        'check_out_at' => '2026-04-20T17:00:00Z',
        'worked_minutes' => 480,
        'break_minutes' => 0,
        'approval_status' => 'approved',
        'approved_by_user_id' => $user->id,
        'approved_at' => '2026-04-20T18:00:00Z',
        'business_date' => '2026-04-20',
    ]);

    Appointment::query()->create([
        'merchant_id' => $merchant->id,
        'store_id' => $store->id,
        'customer_id' => null,
        'staff_profile_id' => $staff->id,
        'service_item_id' => $service->id,
        'slot_claim_id' => null,
        'status' => 'completed',
        'status_seq' => 3,
        'source' => 'walk_in',
        'business_date' => '2026-04-20',
        'starts_at' => '2026-04-20T10:00:00Z',
        'ends_at' => '2026-04-20T11:30:00Z',
        'checked_in_at' => '2026-04-20T09:55:00Z',
        'completed_at' => '2026-04-20T11:35:00Z',
        'service_name' => 'Hair Coloring',
        'service_price_minor' => 6000,
        'discount_minor' => 500,
        'currency' => 'USD',
    ]);

    $snapshot = $this->postJson("/api/admin/v1/stores/{$store->id}/workforce/payroll-snapshots/generate", [
        'week_reference_date' => '2026-04-21',
    ])->assertCreated()
        ->assertJsonPath('data.period_type', 'weekly')
        ->assertJsonPath('data.approved_minutes', 480)
        ->assertJsonPath('data.gross_wages_minor', 24000)
        ->assertJsonPath('data.gross_commission_minor', 825)
        ->assertJsonPath('data.gross_pay_minor', 24825)
        ->json('data');

    $this->getJson("/api/admin/v1/stores/{$store->id}/workforce/payroll-snapshots/{$snapshot['id']}")
        ->assertOk()
        ->assertJsonPath('data.id', $snapshot['id'])
        ->assertJsonPath('data.gross_pay_minor', 24825);

    $this->getJson("/api/admin/v1/stores/{$store->id}/workforce/labor-analytics?start_date=2026-04-20&end_date=2026-04-26")
        ->assertOk()
        ->assertJsonPath('data.approved_minutes', 480)
        ->assertJsonPath('data.appointments_completed_count', 1)
        ->assertJsonPath('data.estimated_labor_cost_minor', 24825);
});
