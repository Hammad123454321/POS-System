<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('payment_splits', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('order_id')->constrained('orders')->cascadeOnDelete();
            $table->foreignUlid('payment_id')->constrained('payments')->cascadeOnDelete();
            $table->unsignedSmallInteger('split_sequence');
            $table->string('method');
            $table->bigInteger('applied_minor');
            $table->bigInteger('tip_minor')->default(0);
            $table->string('status')->default('captured');
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->unique(['order_id', 'split_sequence']);
            $table->index(['merchant_id', 'store_id', 'method', 'created_at']);
        });

        Schema::create('exception_resolutions', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignUlid('exception_case_id')->constrained('exception_cases')->cascadeOnDelete();
            $table->foreignId('resolved_by_user_id')->constrained('users');
            $table->string('resolution_code');
            $table->text('notes')->nullable();
            $table->timestampTz('resolved_at');
            $table->timestamps();

            $table->unique(['exception_case_id']);
            $table->index(['merchant_id', 'store_id', 'resolved_at']);
        });

        Schema::create('feature_flags', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->string('flag_key');
            $table->boolean('is_enabled')->default(false);
            $table->text('value_json')->nullable();
            $table->boolean('is_self_service')->default(false);
            $table->foreignId('updated_by_user_id')->nullable()->constrained('users');
            $table->timestamps();

            $table->unique(['merchant_id', 'flag_key']);
            $table->index(['merchant_id', 'is_enabled']);
        });

        Schema::create('feature_flag_overrides', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('flag_key');
            $table->boolean('is_enabled')->default(false);
            $table->text('value_json')->nullable();
            $table->foreignId('updated_by_user_id')->nullable()->constrained('users');
            $table->timestamps();

            $table->unique(['store_id', 'flag_key']);
            $table->index(['merchant_id', 'store_id', 'is_enabled']);
        });

        Schema::create('archive_access_logs', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->string('archive_type');
            $table->string('archive_record_id');
            $table->foreignId('accessed_by_user_id')->nullable()->constrained('users');
            $table->foreignUlid('accessed_by_device_id')->nullable()->constrained('devices');
            $table->string('reason')->nullable();
            $table->json('metadata')->nullable();
            $table->timestampTz('accessed_at');
            $table->timestamps();

            $table->index(['merchant_id', 'archive_type', 'accessed_at']);
        });

        Schema::create('device_status_events', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('device_id')->constrained('devices')->cascadeOnDelete();
            $table->string('status');
            $table->json('metadata')->nullable();
            $table->timestampTz('occurred_at');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'status', 'occurred_at']);
        });

        if (DB::getDriverName() === 'pgsql') {
            $this->applyMerchantPolicies([
                'payment_splits',
                'exception_resolutions',
                'feature_flags',
                'feature_flag_overrides',
                'archive_access_logs',
                'device_status_events',
            ]);
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('device_status_events');
        Schema::dropIfExists('archive_access_logs');
        Schema::dropIfExists('feature_flag_overrides');
        Schema::dropIfExists('feature_flags');
        Schema::dropIfExists('exception_resolutions');
        Schema::dropIfExists('payment_splits');
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
