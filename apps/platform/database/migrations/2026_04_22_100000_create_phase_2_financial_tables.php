<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('membership_plans', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->string('name');
            $table->string('code')->nullable();
            $table->bigInteger('price_minor');
            $table->char('currency', 3);
            $table->unsignedInteger('duration_days');
            $table->json('benefits_snapshot')->nullable();
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['merchant_id', 'created_at']);
            $table->unique(['merchant_id', 'code']);
        });

        Schema::create('membership_ledger_entries', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignUlid('member_account_id')->constrained('member_accounts')->cascadeOnDelete();
            $table->foreignUlid('membership_plan_id')->nullable()->constrained('membership_plans');
            $table->foreignUlid('customer_id')->nullable()->constrained('customers');
            $table->foreignUlid('device_id')->nullable()->constrained('devices');
            $table->string('entry_type');
            $table->bigInteger('amount_minor')->default(0);
            $table->json('metadata')->nullable();
            $table->timestampTz('effective_starts_at')->nullable();
            $table->timestampTz('effective_ends_at')->nullable();
            $table->timestampTz('occurred_at');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'occurred_at']);
        });

        Schema::table('member_accounts', function (Blueprint $table): void {
            $table->foreignUlid('membership_plan_id')->nullable()->after('customer_id')->constrained('membership_plans');
            $table->timestampTz('valid_from')->nullable()->after('status');
            $table->timestampTz('valid_until')->nullable()->after('valid_from');
            $table->json('benefits_snapshot')->nullable()->after('valid_until');
        });

        Schema::create('gift_cards', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('issued_to_customer_id')->nullable()->constrained('customers');
            $table->string('code');
            $table->char('currency', 3);
            $table->string('status')->default('active');
            $table->bigInteger('current_balance_minor')->default(0);
            $table->timestampTz('activated_at')->nullable();
            $table->timestampTz('last_used_at')->nullable();
            $table->timestamps();

            $table->unique(['merchant_id', 'code']);
            $table->index(['merchant_id', 'created_at']);
        });

        Schema::create('gift_card_ledger_entries', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignUlid('gift_card_id')->constrained('gift_cards')->cascadeOnDelete();
            $table->foreignUlid('order_id')->nullable()->constrained('orders');
            $table->foreignUlid('payment_id')->nullable()->constrained('payments');
            $table->foreignUlid('device_id')->nullable()->constrained('devices');
            $table->string('entry_type');
            $table->bigInteger('amount_minor');
            $table->bigInteger('balance_after_minor');
            $table->json('metadata')->nullable();
            $table->timestampTz('occurred_at');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'occurred_at']);
        });

        Schema::create('spend_holds', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignUlid('device_id')->nullable()->constrained('devices');
            $table->foreignUlid('order_id')->nullable()->constrained('orders');
            $table->string('resource_type');
            $table->string('resource_id');
            $table->bigInteger('amount_minor');
            $table->string('status')->default('active');
            $table->timestampTz('heartbeat_at')->nullable();
            $table->timestampTz('expires_at');
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'resource_type', 'resource_id']);
            $table->index(['status', 'expires_at']);
        });

        Schema::create('refunds', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('order_id')->nullable()->constrained('orders');
            $table->foreignUlid('payment_id')->constrained('payments');
            $table->foreignUlid('device_id')->nullable()->constrained('devices');
            $table->bigInteger('amount_minor');
            $table->string('status')->default('completed');
            $table->string('provider_refund_id')->nullable();
            $table->string('reason')->nullable();
            $table->json('metadata')->nullable();
            $table->timestampTz('refunded_at');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'refunded_at']);
        });

        Schema::create('void_records', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('order_id')->nullable()->constrained('orders');
            $table->foreignUlid('payment_id')->constrained('payments');
            $table->foreignUlid('device_id')->nullable()->constrained('devices');
            $table->string('status')->default('completed');
            $table->string('provider_void_id')->nullable();
            $table->string('reason')->nullable();
            $table->json('metadata')->nullable();
            $table->timestampTz('voided_at');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'voided_at']);
        });

        Schema::create('audit_logs', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignId('user_id')->nullable()->constrained('users');
            $table->foreignUlid('device_id')->nullable()->constrained('devices');
            $table->string('module');
            $table->string('action');
            $table->string('subject_type')->nullable();
            $table->string('subject_id')->nullable();
            $table->json('before_state')->nullable();
            $table->json('after_state')->nullable();
            $table->json('metadata')->nullable();
            $table->timestampTz('occurred_at');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'occurred_at']);
        });

        Schema::create('usage_records', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->string('metric_key');
            $table->bigInteger('quantity')->default(0);
            $table->date('recorded_on');
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'metric_key', 'recorded_on']);
        });

        Schema::create('sync_recovery_runs', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('initiated_by_device_id')->nullable()->constrained('devices');
            $table->string('batch_id')->nullable();
            $table->string('status')->default('queued');
            $table->unsignedInteger('event_count')->default(0);
            $table->timestampTz('started_at');
            $table->timestampTz('finished_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'started_at']);
        });

        Schema::table('orders', function (Blueprint $table): void {
            $table->bigInteger('tip_minor')->default(0)->after('discount_minor');
        });

        Schema::table('payments', function (Blueprint $table): void {
            $table->string('provider_key')->nullable()->after('method');
            $table->string('provider_transaction_id')->nullable()->after('provider_key');
            $table->string('terminal_reference')->nullable()->after('provider_transaction_id');
            $table->bigInteger('applied_minor')->default(0)->after('amount_minor');
            $table->bigInteger('tip_minor')->default(0)->after('applied_minor');
            $table->bigInteger('refundable_minor')->default(0)->after('tip_minor');
            $table->timestampTz('authorized_at')->nullable()->after('captured_at');
            $table->timestampTz('voided_at')->nullable()->after('authorized_at');
            $table->timestampTz('refunded_at')->nullable()->after('voided_at');
        });

        Schema::table('sync_events', function (Blueprint $table): void {
            $table->unsignedInteger('recovery_attempts')->default(0)->after('error_code');
            $table->string('recovery_batch_id')->nullable()->after('recovery_attempts');
        });

        if (DB::getDriverName() === 'pgsql') {
            $this->applyMerchantPolicies([
                'membership_plans',
                'membership_ledger_entries',
                'gift_cards',
                'gift_card_ledger_entries',
                'spend_holds',
                'refunds',
                'void_records',
                'audit_logs',
                'usage_records',
                'sync_recovery_runs',
            ]);
        }
    }

    public function down(): void
    {
        Schema::table('sync_events', function (Blueprint $table): void {
            $table->dropColumn(['recovery_attempts', 'recovery_batch_id']);
        });

        Schema::table('payments', function (Blueprint $table): void {
            $table->dropColumn([
                'provider_key',
                'provider_transaction_id',
                'terminal_reference',
                'applied_minor',
                'tip_minor',
                'refundable_minor',
                'authorized_at',
                'voided_at',
                'refunded_at',
            ]);
        });

        Schema::table('orders', function (Blueprint $table): void {
            $table->dropColumn('tip_minor');
        });

        Schema::dropIfExists('sync_recovery_runs');
        Schema::dropIfExists('usage_records');
        Schema::dropIfExists('audit_logs');
        Schema::dropIfExists('void_records');
        Schema::dropIfExists('refunds');
        Schema::dropIfExists('spend_holds');
        Schema::dropIfExists('gift_card_ledger_entries');
        Schema::dropIfExists('gift_cards');

        Schema::table('member_accounts', function (Blueprint $table): void {
            $table->dropConstrainedForeignId('membership_plan_id');
            $table->dropColumn(['valid_from', 'valid_until', 'benefits_snapshot']);
        });

        Schema::dropIfExists('membership_ledger_entries');
        Schema::dropIfExists('membership_plans');
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
