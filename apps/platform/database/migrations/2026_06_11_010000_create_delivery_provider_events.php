<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('delivery_provider_events', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants')->nullOnDelete();
            $table->foreignUlid('store_id')->nullable()->constrained('stores')->nullOnDelete();
            $table->string('channel_key');
            $table->string('external_event_id');
            $table->string('external_order_id')->nullable();
            $table->string('event_type')->nullable();
            $table->boolean('signature_valid')->default(false);
            $table->json('payload')->nullable();
            $table->timestampTz('processed_at')->nullable();
            $table->foreignUlid('external_order_link_id')->nullable()->constrained('external_order_links')->nullOnDelete();
            $table->timestamps();

            $table->unique(['channel_key', 'external_event_id']);
            $table->index(['merchant_id', 'store_id', 'created_at']);
        });

        // Normalized external store identifier for fast webhook → store resolution.
        Schema::table('delivery_channel_configs', function (Blueprint $table): void {
            $table->string('external_store_id')->nullable()->after('channel_key');
            $table->index(['channel_key', 'external_store_id']);
        });

        // Backfill from the existing mapping JSON where present. Note: the `mapping`
        // column is `json` (not `jsonb`), so use the text-extraction operator and a
        // NULL check rather than the `?` existence operator (which PDO would treat
        // as a bind placeholder).
        if (DB::getDriverName() === 'pgsql') {
            DB::unprepared("UPDATE delivery_channel_configs
                SET external_store_id = mapping->>'external_store_id'
                WHERE mapping->>'external_store_id' IS NOT NULL");
        }

        if (DB::getDriverName() === 'pgsql') {
            DB::statement('ALTER TABLE delivery_provider_events ENABLE ROW LEVEL SECURITY');
            DB::statement('ALTER TABLE delivery_provider_events FORCE ROW LEVEL SECURITY');
            DB::statement('DROP POLICY IF EXISTS delivery_provider_events_merchant_isolation ON delivery_provider_events');
            DB::statement(
                "CREATE POLICY delivery_provider_events_merchant_isolation ON delivery_provider_events
                USING (merchant_id IS NULL OR merchant_id = current_setting('app.current_merchant_id', true))
                WITH CHECK (merchant_id IS NULL OR merchant_id = current_setting('app.current_merchant_id', true))"
            );
        }
    }

    public function down(): void
    {
        Schema::table('delivery_channel_configs', function (Blueprint $table): void {
            $table->dropIndex(['channel_key', 'external_store_id']);
            $table->dropColumn('external_store_id');
        });

        Schema::dropIfExists('delivery_provider_events');
    }
};
