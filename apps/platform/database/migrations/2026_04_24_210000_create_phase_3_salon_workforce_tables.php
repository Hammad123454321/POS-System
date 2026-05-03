<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('service_items', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('catalog_item_id')->constrained('catalog_items')->cascadeOnDelete();
            $table->unsignedSmallInteger('duration_minutes');
            $table->unsignedSmallInteger('buffer_minutes')->default(0);
            $table->boolean('is_walk_in_enabled')->default(true);
            $table->timestamps();

            $table->unique(['catalog_item_id']);
            $table->index(['merchant_id', 'created_at']);
        });

        Schema::create('staff_profiles', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('display_name');
            $table->string('code')->nullable();
            $table->string('role_title')->nullable();
            $table->string('phone')->nullable();
            $table->string('email')->nullable();
            $table->string('status')->default('active');
            $table->date('hired_on')->nullable();
            $table->date('terminated_on')->nullable();
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->unique(['store_id', 'code']);
            $table->index(['merchant_id', 'store_id', 'status', 'created_at']);
        });

        Schema::create('commission_rules', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('name');
            $table->string('base_type')->default('service_net');
            $table->unsignedInteger('rate_basis_points')->nullable();
            $table->bigInteger('fixed_minor')->nullable();
            $table->char('currency', 3);
            $table->date('effective_from')->nullable();
            $table->date('effective_to')->nullable();
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'is_active', 'effective_from']);
        });

        Schema::create('wage_rules', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('staff_profile_id')->constrained('staff_profiles')->cascadeOnDelete();
            $table->string('name');
            $table->string('wage_type')->default('hourly');
            $table->bigInteger('hourly_rate_minor');
            $table->char('currency', 3);
            $table->date('effective_from');
            $table->date('effective_to')->nullable();
            $table->boolean('is_active')->default(true);
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'staff_profile_id', 'effective_from']);
        });

        Schema::create('staff_service_rules', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('staff_profile_id')->constrained('staff_profiles')->cascadeOnDelete();
            $table->foreignUlid('service_item_id')->constrained('service_items')->cascadeOnDelete();
            $table->foreignUlid('commission_rule_id')->nullable()->constrained('commission_rules');
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->unique(['staff_profile_id', 'service_item_id']);
            $table->index(['merchant_id', 'store_id', 'is_active']);
        });

        Schema::create('appointments', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('customer_id')->nullable()->constrained('customers');
            $table->foreignUlid('staff_profile_id')->constrained('staff_profiles');
            $table->foreignUlid('service_item_id')->constrained('service_items');
            $table->foreignUlid('slot_claim_id')->nullable()->constrained('edit_leases');
            $table->string('status')->default('confirmed');
            $table->unsignedInteger('status_seq')->default(1);
            $table->string('source')->default('walk_in');
            $table->date('business_date');
            $table->timestampTz('starts_at');
            $table->timestampTz('ends_at');
            $table->timestampTz('checked_in_at')->nullable();
            $table->timestampTz('completed_at')->nullable();
            $table->timestampTz('cancelled_at')->nullable();
            $table->string('service_name');
            $table->bigInteger('service_price_minor');
            $table->bigInteger('discount_minor')->default(0);
            $table->char('currency', 3);
            $table->text('notes')->nullable();
            $table->json('metadata')->nullable();
            $table->foreignUlid('created_by_device_id')->nullable()->constrained('devices');
            $table->foreignUlid('updated_by_device_id')->nullable()->constrained('devices');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->index(['store_id', 'starts_at']);
            $table->index(['staff_profile_id', 'starts_at']);
            $table->index(['store_id', 'business_date', 'status']);
            $table->index(['slot_claim_id']);
        });

        Schema::create('shifts', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('staff_profile_id')->constrained('staff_profiles');
            $table->foreignUlid('opened_by_device_id')->nullable()->constrained('devices');
            $table->foreignUlid('closed_by_device_id')->nullable()->constrained('devices');
            $table->date('business_date');
            $table->string('status')->default('open');
            $table->unsignedInteger('session_version')->default(1);
            $table->timestampTz('scheduled_start_at')->nullable();
            $table->timestampTz('scheduled_end_at')->nullable();
            $table->timestampTz('started_at');
            $table->timestampTz('ended_at')->nullable();
            $table->unsignedInteger('total_minutes')->nullable();
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->index(['staff_profile_id', 'started_at']);
            $table->index(['store_id', 'business_date', 'status']);
        });

        Schema::create('attendance_records', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('staff_profile_id')->constrained('staff_profiles');
            $table->foreignUlid('shift_id')->nullable()->constrained('shifts')->cascadeOnDelete();
            $table->timestampTz('check_in_at');
            $table->timestampTz('check_out_at')->nullable();
            $table->unsignedInteger('worked_minutes')->nullable();
            $table->unsignedInteger('break_minutes')->default(0);
            $table->string('approval_status')->default('pending');
            $table->foreignId('approved_by_user_id')->nullable()->constrained('users');
            $table->timestampTz('approved_at')->nullable();
            $table->date('business_date');
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->index(['staff_profile_id', 'check_in_at']);
            $table->index(['store_id', 'business_date', 'approval_status']);
        });

        Schema::create('payroll_snapshots', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('period_type')->default('weekly');
            $table->date('period_start');
            $table->date('period_end');
            $table->unsignedInteger('staff_count')->default(0);
            $table->unsignedInteger('approved_minutes')->default(0);
            $table->unsignedInteger('regular_minutes')->default(0);
            $table->unsignedInteger('overtime_minutes')->default(0);
            $table->bigInteger('gross_wages_minor')->default(0);
            $table->bigInteger('gross_commission_minor')->default(0);
            $table->bigInteger('gross_pay_minor')->default(0);
            $table->json('payload')->nullable();
            $table->foreignId('generated_by_user_id')->nullable()->constrained('users');
            $table->timestampTz('generated_at');
            $table->timestamps();

            $table->unique(['store_id', 'period_type', 'period_start', 'period_end']);
            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->index(['store_id', 'period_start']);
        });

        if (DB::getDriverName() === 'pgsql') {
            DB::statement('CREATE EXTENSION IF NOT EXISTS btree_gist');

            DB::statement(
                "ALTER TABLE appointments
                ADD CONSTRAINT appointments_no_overlapping_staff_bookings
                EXCLUDE USING gist (
                    staff_profile_id WITH =,
                    tstzrange(starts_at, ends_at, '[)') WITH &&
                )
                WHERE (status IN ('confirmed', 'checked_in'))"
            );

            $this->applyMerchantPolicies([
                'service_items',
                'staff_profiles',
                'commission_rules',
                'wage_rules',
                'staff_service_rules',
                'appointments',
                'shifts',
                'attendance_records',
                'payroll_snapshots',
            ]);
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('payroll_snapshots');
        Schema::dropIfExists('attendance_records');
        Schema::dropIfExists('shifts');
        Schema::dropIfExists('appointments');
        Schema::dropIfExists('staff_service_rules');
        Schema::dropIfExists('wage_rules');
        Schema::dropIfExists('commission_rules');
        Schema::dropIfExists('staff_profiles');
        Schema::dropIfExists('service_items');
    }

    /**
     * @param  array<int, string>  $tables
     */
    private function applyMerchantPolicies(array $tables): void
    {
        foreach ($tables as $table) {
            DB::statement("ALTER TABLE {$table} ENABLE ROW LEVEL SECURITY");
            DB::statement("ALTER TABLE {$table} FORCE ROW LEVEL SECURITY");
            DB::statement("DROP POLICY IF EXISTS {$table}_merchant_isolation ON {$table}");
            DB::statement(
                "CREATE POLICY {$table}_merchant_isolation ON {$table}
                USING (merchant_id = current_setting('app.current_merchant_id', true))
                WITH CHECK (merchant_id = current_setting('app.current_merchant_id', true))"
            );
        }
    }
};
