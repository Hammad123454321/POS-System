<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('payment_provider_events', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->foreignUlid('store_id')->nullable()->constrained('stores');
            $table->foreignUlid('payment_id')->nullable()->constrained('payments')->nullOnDelete();
            $table->string('provider_key');
            $table->string('provider_account_id')->nullable();
            $table->string('provider_transaction_id');
            $table->string('event_type');
            $table->string('event_status')->nullable();
            $table->boolean('signature_valid')->nullable();
            $table->json('payload')->nullable();
            $table->timestampTz('occurred_at');
            $table->timestamps();

            $table->unique(
                ['provider_key', 'provider_transaction_id', 'event_type'],
                'payment_provider_events_event_unique',
            );
            $table->index(['merchant_id', 'store_id', 'occurred_at']);
            $table->index(['provider_key', 'provider_transaction_id']);
        });

        if (DB::getDriverName() === 'pgsql') {
            DB::statement('ALTER TABLE payment_provider_events ENABLE ROW LEVEL SECURITY');
            DB::statement('ALTER TABLE payment_provider_events FORCE ROW LEVEL SECURITY');
            DB::statement('DROP POLICY IF EXISTS payment_provider_events_merchant_isolation ON payment_provider_events');
            DB::statement(
                'CREATE POLICY payment_provider_events_merchant_isolation ON payment_provider_events
                USING (merchant_id IS NULL OR merchant_id = current_setting(\'app.current_merchant_id\', true))
                WITH CHECK (merchant_id IS NULL OR merchant_id = current_setting(\'app.current_merchant_id\', true))'
            );
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('payment_provider_events');
    }
};
