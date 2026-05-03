<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('customers', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->string('name');
            $table->string('phone')->nullable();
            $table->string('email')->nullable();
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['merchant_id', 'created_at']);
            $table->index(['merchant_id', 'phone']);
            $table->index(['merchant_id', 'email']);
        });

        Schema::create('member_accounts', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('customer_id')->constrained('customers')->cascadeOnDelete();
            $table->string('member_number');
            $table->string('status')->default('active');
            $table->timestamps();

            $table->unique(['merchant_id', 'member_number']);
            $table->unique(['customer_id']);
            $table->index(['merchant_id', 'status', 'created_at']);
        });

        Schema::create('discount_rules', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->string('name');
            $table->string('code')->nullable();
            $table->string('type');
            $table->bigInteger('value_minor')->nullable();
            $table->unsignedInteger('value_basis_points')->nullable();
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'sort_order']);
            $table->unique(['merchant_id', 'code']);
        });

        Schema::create('printer_configs', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('name');
            $table->string('driver_key');
            $table->json('connection_config')->nullable();
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
        });

        Schema::create('print_routes', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->string('route_key');
            $table->string('document_type')->default('receipt');
            $table->foreignUlid('primary_printer_config_id')->constrained('printer_configs');
            $table->foreignUlid('secondary_printer_config_id')->nullable()->constrained('printer_configs');
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->unique(['store_id', 'route_key']);
            $table->index(['merchant_id', 'store_id', 'document_type']);
        });

        Schema::table('orders', function (Blueprint $table): void {
            $table->foreignUlid('customer_id')->nullable()->after('store_id')->constrained('customers');
            $table->foreignUlid('member_account_id')->nullable()->after('customer_id')->constrained('member_accounts');
            $table->foreignUlid('discount_rule_id')->nullable()->after('member_account_id')->constrained('discount_rules');
            $table->json('discount_snapshot')->nullable()->after('discount_rule_id');
        });

        Schema::table('order_lines', function (Blueprint $table): void {
            $table->bigInteger('discount_minor')->default(0)->after('subtotal_minor');
        });

        if (DB::getDriverName() === 'pgsql') {
            $this->applyMerchantPolicies([
                'customers',
                'member_accounts',
                'discount_rules',
                'printer_configs',
                'print_routes',
            ]);
        }
    }

    public function down(): void
    {
        Schema::table('order_lines', function (Blueprint $table): void {
            $table->dropColumn('discount_minor');
        });

        Schema::table('orders', function (Blueprint $table): void {
            $table->dropConstrainedForeignId('discount_rule_id');
            $table->dropConstrainedForeignId('member_account_id');
            $table->dropConstrainedForeignId('customer_id');
            $table->dropColumn('discount_snapshot');
        });

        Schema::dropIfExists('print_routes');
        Schema::dropIfExists('printer_configs');
        Schema::dropIfExists('discount_rules');
        Schema::dropIfExists('member_accounts');
        Schema::dropIfExists('customers');
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
