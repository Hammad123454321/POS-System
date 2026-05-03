<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('variants', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignUlid('catalog_item_id')->constrained('catalog_items')->cascadeOnDelete();
            $table->string('name');
            $table->string('code')->nullable();
            $table->json('options')->nullable();
            $table->bigInteger('price_delta_minor')->default(0);
            $table->char('currency', 3)->nullable();
            $table->boolean('is_active')->default(true);
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->index(['catalog_item_id', 'sort_order']);
            $table->unique(['catalog_item_id', 'code']);
        });

        Schema::create('modifier_groups', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignUlid('catalog_item_id')->constrained('catalog_items')->cascadeOnDelete();
            $table->string('name');
            $table->string('selection_mode')->default('multi');
            $table->unsignedSmallInteger('min_select')->default(0);
            $table->unsignedSmallInteger('max_select')->nullable();
            $table->boolean('is_required')->default(false);
            $table->boolean('is_active')->default(true);
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->index(['catalog_item_id', 'sort_order']);
        });

        Schema::create('modifier_options', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignUlid('modifier_group_id')->constrained('modifier_groups')->cascadeOnDelete();
            $table->string('name');
            $table->string('code')->nullable();
            $table->bigInteger('price_delta_minor')->default(0);
            $table->char('currency', 3)->nullable();
            $table->boolean('is_active')->default(true);
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->index(['modifier_group_id', 'sort_order']);
            $table->unique(['modifier_group_id', 'code']);
        });

        Schema::create('combo_packages', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->string('name');
            $table->string('code')->nullable();
            $table->bigInteger('price_minor');
            $table->char('currency', 3);
            $table->boolean('is_active')->default(true);
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->unique(['merchant_id', 'code']);
        });

        Schema::create('combo_package_items', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('combo_package_id')->constrained('combo_packages')->cascadeOnDelete();
            $table->foreignUlid('catalog_item_id')->constrained('catalog_items')->cascadeOnDelete();
            $table->unsignedInteger('quantity')->default(1);
            $table->timestamps();

            $table->unique(['combo_package_id', 'catalog_item_id']);
            $table->index(['merchant_id', 'created_at']);
        });

        Schema::create('combo_package_add_ons', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('combo_package_id')->constrained('combo_packages')->cascadeOnDelete();
            $table->foreignUlid('catalog_item_id')->constrained('catalog_items')->cascadeOnDelete();
            $table->timestamps();

            $table->unique(['combo_package_id', 'catalog_item_id']);
            $table->index(['merchant_id', 'created_at']);
        });

        Schema::create('catalog_item_add_ons', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('catalog_item_id')->constrained('catalog_items')->cascadeOnDelete();
            $table->foreignUlid('add_on_item_id')->constrained('catalog_items')->cascadeOnDelete();
            $table->timestamps();

            $table->unique(['catalog_item_id', 'add_on_item_id']);
            $table->index(['merchant_id', 'created_at']);
        });

        Schema::table('order_lines', function (Blueprint $table): void {
            $table->json('selection_snapshot')->nullable()->after('discount_snapshot');
        });

        if (DB::getDriverName() === 'pgsql') {
            $this->applyMerchantPolicies([
                'variants',
                'modifier_groups',
                'modifier_options',
                'combo_packages',
                'combo_package_items',
                'combo_package_add_ons',
                'catalog_item_add_ons',
            ]);
        }
    }

    public function down(): void
    {
        Schema::table('order_lines', function (Blueprint $table): void {
            $table->dropColumn('selection_snapshot');
        });

        Schema::dropIfExists('catalog_item_add_ons');
        Schema::dropIfExists('combo_package_add_ons');
        Schema::dropIfExists('combo_package_items');
        Schema::dropIfExists('combo_packages');
        Schema::dropIfExists('modifier_options');
        Schema::dropIfExists('modifier_groups');
        Schema::dropIfExists('variants');
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
