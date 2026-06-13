<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('orders', function (Blueprint $table): void {
            $table->unsignedInteger('status_seq')->default(1)->after('status');
        });

        Schema::table('sync_events', function (Blueprint $table): void {
            $table->string('conflict_code')->nullable()->after('error_code');
        });

        Schema::create('inventory_ledger_entries', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('sku');
            $table->foreignUlid('catalog_item_id')->nullable();
            $table->bigInteger('seq');
            $table->integer('delta_quantity');
            $table->string('reason');
            $table->string('source_type')->nullable();
            $table->string('source_id')->nullable();
            $table->foreignUlid('device_id')->nullable();
            $table->timestampTz('occurred_at');
            $table->timestamps();

            $table->unique(['store_id', 'sku', 'seq']);
            $table->index(['merchant_id', 'store_id', 'sku']);
        });

        if (DB::getDriverName() === 'pgsql') {
            DB::statement('ALTER TABLE inventory_ledger_entries ENABLE ROW LEVEL SECURITY');
            DB::statement('ALTER TABLE inventory_ledger_entries FORCE ROW LEVEL SECURITY');
            DB::statement('DROP POLICY IF EXISTS inventory_ledger_entries_merchant_isolation ON inventory_ledger_entries');
            DB::statement(
                "CREATE POLICY inventory_ledger_entries_merchant_isolation ON inventory_ledger_entries
                USING (merchant_id = current_setting('app.current_merchant_id', true))
                WITH CHECK (merchant_id = current_setting('app.current_merchant_id', true))"
            );
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('inventory_ledger_entries');

        Schema::table('sync_events', function (Blueprint $table): void {
            $table->dropColumn('conflict_code');
        });

        Schema::table('orders', function (Blueprint $table): void {
            $table->dropColumn('status_seq');
        });
    }
};
