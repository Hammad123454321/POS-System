<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('report_daily_store_summaries', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->date('business_date');
            $table->unsignedInteger('orders_count')->default(0);
            $table->bigInteger('gross_minor')->default(0);
            $table->bigInteger('tax_minor')->default(0);
            $table->bigInteger('discount_minor')->default(0);
            $table->bigInteger('net_minor')->default(0);
            $table->json('tender_breakdown')->nullable();
            $table->timestampTz('last_aggregated_at')->nullable();
            $table->boolean('is_final')->default(false);
            $table->timestamps();

            $table->unique(['store_id', 'business_date']);
            $table->index(['merchant_id', 'business_date']);
        });

        if (DB::getDriverName() === 'pgsql') {
            DB::statement('ALTER TABLE report_daily_store_summaries ENABLE ROW LEVEL SECURITY');
            DB::statement('ALTER TABLE report_daily_store_summaries FORCE ROW LEVEL SECURITY');
            DB::statement('DROP POLICY IF EXISTS report_daily_store_summaries_merchant_isolation ON report_daily_store_summaries');
            DB::statement(
                "CREATE POLICY report_daily_store_summaries_merchant_isolation ON report_daily_store_summaries
                USING (merchant_id = current_setting('app.current_merchant_id', true))
                WITH CHECK (merchant_id = current_setting('app.current_merchant_id', true))"
            );
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('report_daily_store_summaries');
    }
};
