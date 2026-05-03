<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('delivery_channel_configs', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('channel_key');
            $table->boolean('is_enabled')->default(false);
            $table->json('credentials')->nullable();
            $table->json('mapping')->nullable();
            $table->json('pause_windows')->nullable();
            $table->unsignedInteger('default_prep_time_minutes')->default(20);
            $table->boolean('sync_hours_enabled')->default(true);
            $table->boolean('sync_prep_time_enabled')->default(true);
            $table->boolean('sync_menu_enabled')->default(true);
            $table->timestampTz('last_menu_published_at')->nullable();
            $table->timestamps();

            $table->unique(['store_id', 'channel_key']);
            $table->index(['merchant_id', 'store_id', 'created_at']);
        });

        Schema::create('external_order_links', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('channel_key');
            $table->string('external_order_id');
            $table->string('external_store_id')->nullable();
            $table->foreignUlid('order_id')->nullable()->constrained('orders')->nullOnDelete();
            $table->string('status')->default('received');
            $table->json('payload')->nullable();
            $table->timestampTz('received_at');
            $table->timestampTz('processed_at')->nullable();
            $table->timestamps();

            $table->unique(['store_id', 'channel_key', 'external_order_id']);
            $table->index(['merchant_id', 'store_id', 'status', 'received_at']);
        });

        Schema::create('inventory_balances', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('catalog_item_id')->nullable()->constrained('catalog_items')->nullOnDelete();
            $table->string('sku');
            $table->integer('on_hand_quantity')->default(0);
            $table->integer('reserved_quantity')->default(0);
            $table->integer('available_quantity')->default(0);
            $table->unsignedBigInteger('inventory_ledger_seq')->default(0);
            $table->string('last_count_session_id')->nullable();
            $table->timestampTz('last_count_closed_at')->nullable();
            $table->timestamps();

            $table->unique(['store_id', 'sku']);
            $table->index(['merchant_id', 'store_id', 'created_at']);
        });

        Schema::create('inventory_adjustments', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('inventory_balance_id')->nullable()->constrained('inventory_balances')->nullOnDelete();
            $table->string('sku');
            $table->string('adjustment_type');
            $table->integer('quantity_delta');
            $table->integer('quantity_before');
            $table->integer('quantity_after');
            $table->string('count_session_id')->nullable();
            $table->timestampTz('count_closed_at')->nullable();
            $table->string('document_number')->nullable();
            $table->string('reason')->nullable();
            $table->string('reference_type')->nullable();
            $table->string('reference_id')->nullable();
            $table->json('metadata')->nullable();
            $table->foreignUlid('created_by_device_id')->nullable()->constrained('devices')->nullOnDelete();
            $table->foreignId('created_by_user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->index(['store_id', 'sku', 'created_at']);
        });

        Schema::create('inventory_transfers', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('source_store_id')->constrained('stores');
            $table->foreignUlid('destination_store_id')->constrained('stores');
            $table->string('document_number');
            $table->string('status')->default('posted');
            $table->json('lines');
            $table->string('reason')->nullable();
            $table->foreignUlid('created_by_device_id')->nullable()->constrained('devices')->nullOnDelete();
            $table->foreignId('created_by_user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->timestampTz('transferred_at');
            $table->timestamps();

            $table->unique(['merchant_id', 'document_number']);
            $table->index(['merchant_id', 'source_store_id', 'transferred_at']);
            $table->index(['merchant_id', 'destination_store_id', 'transferred_at']);
        });

        Schema::create('receiving_records', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('document_number');
            $table->string('status')->default('posted');
            $table->json('lines');
            $table->string('supplier_name')->nullable();
            $table->string('reason')->nullable();
            $table->foreignUlid('created_by_device_id')->nullable()->constrained('devices')->nullOnDelete();
            $table->foreignId('created_by_user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->timestampTz('received_at');
            $table->timestamps();

            $table->unique(['store_id', 'document_number']);
            $table->index(['merchant_id', 'store_id', 'received_at']);
        });

        Schema::create('barcode_records', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('catalog_item_id')->nullable()->constrained('catalog_items')->nullOnDelete();
            $table->string('sku');
            $table->string('barcode');
            $table->boolean('is_primary')->default(false);
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->unique(['store_id', 'barcode']);
            $table->index(['merchant_id', 'store_id', 'sku']);
        });

        if (DB::getDriverName() === 'pgsql') {
            $this->applyMerchantPolicies([
                'delivery_channel_configs',
                'external_order_links',
                'inventory_balances',
                'inventory_adjustments',
                'inventory_transfers',
                'receiving_records',
                'barcode_records',
            ]);
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('barcode_records');
        Schema::dropIfExists('receiving_records');
        Schema::dropIfExists('inventory_transfers');
        Schema::dropIfExists('inventory_adjustments');
        Schema::dropIfExists('inventory_balances');
        Schema::dropIfExists('external_order_links');
        Schema::dropIfExists('delivery_channel_configs');
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
