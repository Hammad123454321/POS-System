<?php

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\SalonWorkforce\Domain\Models\Appointment;
use App\Modules\SalonWorkforce\Domain\Models\AttendanceRecord;
use App\Modules\SalonWorkforce\Domain\Models\ServiceItem;
use App\Modules\SalonWorkforce\Domain\Models\Shift;
use App\Modules\SalonWorkforce\Domain\Models\StaffProfile;
use App\Modules\SalonWorkforce\Domain\Models\WageRule;
use Laravel\Sanctum\Sanctum;

function buildPhase3WorkforcePosContext(): array
{
    [$device, $registerSession] = buildPosOrderContext();

    /** @var Device $device */
    $device = $device;

    $staff = StaffProfile::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'display_name' => 'Phase3 Stylist',
        'code' => 'P3-STY-1',
        'status' => 'active',
    ]);

    $serviceCatalogItem = CatalogItem::query()->create([
        'merchant_id' => $device->merchant_id,
        'category_id' => null,
        'tax_rule_id' => null,
        'type' => 'service',
        'name' => 'Deluxe Cut',
        'sku' => 'SRV-DELUXE',
        'base_price_minor' => 5500,
        'currency' => 'USD',
        'is_active' => true,
        'sold_out' => false,
    ]);

    $serviceItem = ServiceItem::query()->create([
        'merchant_id' => $device->merchant_id,
        'catalog_item_id' => $serviceCatalogItem->id,
        'duration_minutes' => 60,
        'buffer_minutes' => 10,
        'is_walk_in_enabled' => true,
    ]);

    WageRule::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'staff_profile_id' => $staff->id,
        'name' => 'Hourly',
        'wage_type' => 'hourly',
        'hourly_rate_minor' => 2800,
        'currency' => 'USD',
        'effective_from' => '2026-04-20',
        'is_active' => true,
    ]);

    return [$device, $registerSession, $staff, $serviceItem];
}

it('returns phase 3 workforce config in POS config payloads', function () {
    [$device, , $staff, $serviceItem] = buildPhase3WorkforcePosContext();

    Sanctum::actingAs($device, ['pos:access']);

    $this->withHeaders(posHeaders())
        ->getJson('/api/pos/v1/config')
        ->assertOk()
        ->assertJsonPath('salon_services.0.id', $serviceItem->id)
        ->assertJsonPath('staff_profiles.0.id', $staff->id)
        ->assertJsonPath('wage_rules.0.staff_profile_id', $staff->id);
});

it('creates checks in and completes appointments through phase 3 POS endpoints', function () {
    [$device, , $staff, $serviceItem] = buildPhase3WorkforcePosContext();

    Sanctum::actingAs($device, ['pos:access']);

    $startsAt = '2026-04-25T10:00:00Z';
    $endsAt = '2026-04-25T11:00:00Z';

    $slotClaim = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-3-slot-claim',
    ]))->postJson('/api/pos/v1/workforce/appointments/slot-claims', [
        'staff_profile_id' => $staff->id,
        'starts_at' => $startsAt,
        'ends_at' => $endsAt,
    ])->assertOk()
        ->json('data');

    $appointment = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-3-appointment-create',
    ]))->postJson('/api/pos/v1/workforce/appointments', [
        'slot_claim_id' => $slotClaim['lease_id'],
        'staff_profile_id' => $staff->id,
        'service_item_id' => $serviceItem->id,
        'starts_at' => $startsAt,
        'ends_at' => $endsAt,
        'source' => 'walk_in',
    ])->assertCreated()
        ->assertJsonPath('data.status', 'confirmed')
        ->json('data');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-3-appointment-checkin',
    ]))->postJson("/api/pos/v1/workforce/appointments/{$appointment['id']}/check-in")
        ->assertOk()
        ->assertJsonPath('data.status', 'checked_in');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-3-appointment-complete',
    ]))->postJson("/api/pos/v1/workforce/appointments/{$appointment['id']}/complete")
        ->assertOk()
        ->assertJsonPath('data.status', 'completed');

    expect(Appointment::query()->findOrFail($appointment['id'])->status)->toBe('completed');
});

it('blocks overlapping appointments and opens an exception case', function () {
    [$device, , $staff, $serviceItem] = buildPhase3WorkforcePosContext();

    Appointment::query()->create([
        'merchant_id' => $device->merchant_id,
        'store_id' => $device->store_id,
        'staff_profile_id' => $staff->id,
        'service_item_id' => $serviceItem->id,
        'status' => 'confirmed',
        'status_seq' => 1,
        'source' => 'walk_in',
        'business_date' => '2026-04-25',
        'starts_at' => '2026-04-25T10:00:00Z',
        'ends_at' => '2026-04-25T11:00:00Z',
        'service_name' => 'Deluxe Cut',
        'service_price_minor' => 5500,
        'discount_minor' => 0,
        'currency' => 'USD',
    ]);

    Sanctum::actingAs($device, ['pos:access']);

    $slotClaim = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-3-overlap-slot-claim',
    ]))->postJson('/api/pos/v1/workforce/appointments/slot-claims', [
        'staff_profile_id' => $staff->id,
        'starts_at' => '2026-04-25T10:30:00Z',
        'ends_at' => '2026-04-25T11:30:00Z',
    ])->assertOk()
        ->json('data');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-3-overlap-create',
    ]))->postJson('/api/pos/v1/workforce/appointments', [
        'slot_claim_id' => $slotClaim['lease_id'],
        'staff_profile_id' => $staff->id,
        'service_item_id' => $serviceItem->id,
        'starts_at' => '2026-04-25T10:30:00Z',
        'ends_at' => '2026-04-25T11:30:00Z',
        'source' => 'walk_in',
    ])->assertStatus(422)
        ->assertJsonPath('message', 'This staff member already has a confirmed appointment in the selected time range.');

    expect(ExceptionCase::query()
        ->where('merchant_id', $device->merchant_id)
        ->where('store_id', $device->store_id)
        ->where('code', 'APPOINTMENT_DOUBLE_BOOKED')
        ->count())->toBe(1);
});

it('opens and closes staff shifts and writes attendance records', function () {
    [$device, , $staff] = buildPhase3WorkforcePosContext();

    Sanctum::actingAs($device, ['pos:access']);

    $shift = $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-3-shift-open',
    ]))->postJson('/api/pos/v1/workforce/shifts/open', [
        'staff_profile_id' => $staff->id,
    ])->assertCreated()
        ->assertJsonPath('data.status', 'open')
        ->json('data');

    $this->withHeaders(posHeaders([
        'Idempotency-Key' => 'phase-3-shift-close',
    ]))->postJson("/api/pos/v1/workforce/shifts/{$shift['id']}/close", [
        'session_version' => $shift['session_version'],
        'break_minutes' => 15,
    ])->assertOk()
        ->assertJsonPath('data.status', 'closed');

    expect(Shift::query()->findOrFail($shift['id'])->status)->toBe('closed');
    expect(AttendanceRecord::query()
        ->where('shift_id', $shift['id'])
        ->where('approval_status', 'pending')
        ->exists())->toBeTrue();
});
