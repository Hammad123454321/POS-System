<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('sync_events', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('device_id')->constrained('devices');
            $table->string('local_event_id');
            $table->string('entity_type');
            $table->string('entity_id')->nullable();
            $table->string('action');
            $table->json('payload');
            $table->string('status')->default('accepted');
            $table->string('error_code')->nullable();
            $table->timestampTz('received_at');
            $table->timestampTz('processed_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->unique(['device_id', 'local_event_id']);
        });

        Schema::create('outbox_jobs', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->string('channel');
            $table->json('payload');
            $table->string('status')->default('pending');
            $table->unsignedInteger('attempts')->default(0);
            $table->timestampTz('available_at');
            $table->timestampTz('processed_at')->nullable();
            $table->timestamps();

            $table->index(['channel', 'status', 'available_at']);
        });

        Schema::create('idempotency_records', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->string('scope_type');
            $table->string('scope_id');
            $table->string('request_method');
            $table->string('route_key');
            $table->string('idempotency_key');
            $table->string('request_hash', 64);
            $table->unsignedSmallInteger('response_status');
            $table->json('response_headers')->nullable();
            $table->json('response_body')->nullable();
            $table->timestampTz('expires_at');
            $table->timestamps();

            $table->unique(['scope_type', 'scope_id', 'request_method', 'route_key', 'idempotency_key'], 'idempotency_scope_unique');
            $table->index(['expires_at']);
        });

        if (DB::getDriverName() === 'pgsql') {
            foreach (['sync_events'] as $table) {
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
        Schema::dropIfExists('idempotency_records');
        Schema::dropIfExists('outbox_jobs');
        Schema::dropIfExists('sync_events');
    }
};
