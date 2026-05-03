<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('dining_tables', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('name');
            $table->string('zone_name')->nullable();
            $table->unsignedSmallInteger('capacity')->default(2);
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'sort_order']);
            $table->unique(['store_id', 'name']);
        });

        Schema::create('table_assignments', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('dining_table_id')->constrained('dining_tables')->cascadeOnDelete();
            $table->string('status')->default('available');
            $table->string('current_party_name')->nullable();
            $table->unsignedSmallInteger('guest_count')->nullable();
            $table->foreignUlid('assigned_device_id')->nullable()->constrained('devices');
            $table->timestampTz('occupied_at')->nullable();
            $table->timestampTz('released_at')->nullable();
            $table->timestamps();

            $table->unique(['dining_table_id']);
            $table->index(['merchant_id', 'store_id', 'status', 'updated_at']);
        });

        Schema::create('edit_leases', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->string('resource_type');
            $table->string('resource_id');
            $table->foreignUlid('holder_device_id')->nullable()->constrained('devices');
            $table->unsignedInteger('lease_version')->default(1);
            $table->unsignedSmallInteger('ttl_seconds');
            $table->timestampTz('last_heartbeat_at')->nullable();
            $table->timestampTz('lease_expires_at')->nullable();
            $table->timestampTz('lease_expired_at')->nullable();
            $table->timestamps();

            $table->unique(['resource_type', 'resource_id']);
            $table->index(['merchant_id', 'store_id', 'lease_expires_at']);
        });

        Schema::create('exception_cases', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->string('module');
            $table->string('code');
            $table->string('severity')->default('medium');
            $table->string('status')->default('open');
            $table->string('title');
            $table->json('details')->nullable();
            $table->string('related_type')->nullable();
            $table->string('related_id')->nullable();
            $table->foreignUlid('opened_by_device_id')->nullable()->constrained('devices');
            $table->foreignId('opened_by_user_id')->nullable()->constrained('users');
            $table->timestampTz('resolved_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'status', 'created_at']);
            $table->index(['module', 'code']);
        });

        if (DB::getDriverName() === 'pgsql') {
            foreach (['dining_tables', 'table_assignments', 'edit_leases', 'exception_cases'] as $table) {
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
    }

    public function down(): void
    {
        Schema::dropIfExists('exception_cases');
        Schema::dropIfExists('edit_leases');
        Schema::dropIfExists('table_assignments');
        Schema::dropIfExists('dining_tables');
    }
};
