<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('tax_rules', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->string('name');
            $table->string('code')->nullable();
            $table->unsignedInteger('rate_basis_points')->default(0);
            $table->boolean('is_inclusive')->default(false);
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['merchant_id', 'created_at']);
        });

        Schema::create('categories', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->string('name');
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
        });

        Schema::create('catalog_items', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('category_id')->nullable()->constrained('categories');
            $table->foreignUlid('tax_rule_id')->nullable()->constrained('tax_rules');
            $table->string('type');
            $table->string('name');
            $table->string('sku')->nullable();
            $table->text('description')->nullable();
            $table->bigInteger('base_price_minor');
            $table->char('currency', 3);
            $table->boolean('is_active')->default(true);
            $table->boolean('sold_out')->default(false);
            $table->timestamps();

            $table->index(['merchant_id', 'created_at']);
            $table->unique(['merchant_id', 'sku']);
        });

        Schema::create('price_rules', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('catalog_item_id')->constrained('catalog_items')->cascadeOnDelete();
            $table->string('name');
            $table->string('scope')->default('base');
            $table->bigInteger('price_minor');
            $table->char('currency', 3);
            $table->unsignedSmallInteger('priority')->default(0);
            $table->boolean('is_active')->default(true);
            $table->timestampTz('starts_at')->nullable();
            $table->timestampTz('ends_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'catalog_item_id', 'priority']);
        });

        Schema::create('register_sessions', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('device_id')->constrained('devices');
            $table->string('drawer_code')->nullable();
            $table->date('business_date');
            $table->string('status')->default('open');
            $table->unsignedInteger('session_version')->default(1);
            $table->bigInteger('opening_float_minor')->default(0);
            $table->bigInteger('expected_cash_minor')->default(0);
            $table->bigInteger('counted_cash_minor')->nullable();
            $table->bigInteger('variance_minor')->nullable();
            $table->timestampTz('opened_at');
            $table->timestampTz('closed_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'business_date']);
            $table->unique(['store_id', 'drawer_code', 'status']);
        });

        Schema::create('orders', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('register_session_id')->constrained('register_sessions');
            $table->foreignUlid('device_id')->constrained('devices');
            $table->string('order_number');
            $table->string('status')->default('open');
            $table->date('business_date');
            $table->char('currency', 3);
            $table->bigInteger('subtotal_minor')->default(0);
            $table->bigInteger('tax_minor')->default(0);
            $table->bigInteger('discount_minor')->default(0);
            $table->bigInteger('total_minor')->default(0);
            $table->bigInteger('paid_minor')->default(0);
            $table->timestampTz('opened_at');
            $table->timestampTz('closed_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->unique(['store_id', 'order_number']);
        });

        Schema::create('order_lines', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('order_id')->constrained('orders')->cascadeOnDelete();
            $table->foreignUlid('catalog_item_id')->nullable()->constrained('catalog_items');
            $table->string('name');
            $table->string('sku')->nullable();
            $table->unsignedInteger('quantity');
            $table->bigInteger('unit_price_minor');
            $table->bigInteger('subtotal_minor');
            $table->bigInteger('tax_minor')->default(0);
            $table->bigInteger('total_minor');
            $table->json('tax_rule_snapshot')->nullable();
            $table->timestamps();
        });

        Schema::create('payments', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('order_id')->constrained('orders')->cascadeOnDelete();
            $table->foreignUlid('register_session_id')->constrained('register_sessions');
            $table->foreignUlid('device_id')->constrained('devices');
            $table->string('method');
            $table->string('status')->default('captured');
            $table->bigInteger('amount_minor');
            $table->bigInteger('tendered_minor');
            $table->bigInteger('change_minor')->default(0);
            $table->json('metadata')->nullable();
            $table->timestampTz('captured_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
        });

        Schema::create('receipts', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('order_id')->unique()->constrained('orders')->cascadeOnDelete();
            $table->foreignUlid('payment_id')->nullable()->constrained('payments');
            $table->foreignUlid('register_session_id')->constrained('register_sessions');
            $table->foreignUlid('device_id')->constrained('devices');
            $table->string('receipt_number');
            $table->json('payload');
            $table->timestampTz('printed_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->unique(['store_id', 'receipt_number']);
        });

        Schema::create('cash_movements', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('register_session_id')->constrained('register_sessions')->cascadeOnDelete();
            $table->foreignUlid('device_id')->constrained('devices');
            $table->string('type');
            $table->bigInteger('amount_minor');
            $table->string('reference_type')->nullable();
            $table->string('reference_id')->nullable();
            $table->json('metadata')->nullable();
            $table->timestampTz('occurred_at');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'occurred_at']);
        });

        if (DB::getDriverName() === 'pgsql') {
            $this->applyMerchantPolicies([
                'tax_rules',
                'categories',
                'catalog_items',
                'price_rules',
                'register_sessions',
                'orders',
                'payments',
                'receipts',
                'cash_movements',
            ]);
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('cash_movements');
        Schema::dropIfExists('receipts');
        Schema::dropIfExists('payments');
        Schema::dropIfExists('order_lines');
        Schema::dropIfExists('orders');
        Schema::dropIfExists('register_sessions');
        Schema::dropIfExists('price_rules');
        Schema::dropIfExists('catalog_items');
        Schema::dropIfExists('categories');
        Schema::dropIfExists('tax_rules');
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
