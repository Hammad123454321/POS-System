<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('discount_rules', function (Blueprint $table): void {
            $table->string('scope_mode')->default('all')->after('type');
            $table->json('applicability')->nullable()->after('value_basis_points');
            $table->boolean('is_stackable')->default(false)->after('applicability');
            $table->timestampTz('starts_at')->nullable()->after('is_stackable');
            $table->timestampTz('ends_at')->nullable()->after('starts_at');
            $table->index(['merchant_id', 'scope_mode', 'is_active']);
        });

        Schema::table('order_lines', function (Blueprint $table): void {
            $table->json('discount_snapshot')->nullable()->after('discount_minor');
        });

        Schema::table('delivery_channel_configs', function (Blueprint $table): void {
            $table->json('metadata')->nullable()->after('mapping');
        });
    }

    public function down(): void
    {
        Schema::table('delivery_channel_configs', function (Blueprint $table): void {
            $table->dropColumn('metadata');
        });

        Schema::table('order_lines', function (Blueprint $table): void {
            $table->dropColumn('discount_snapshot');
        });

        Schema::table('discount_rules', function (Blueprint $table): void {
            $table->dropIndex(['merchant_id', 'scope_mode', 'is_active']);
            $table->dropColumn([
                'scope_mode',
                'applicability',
                'is_stackable',
                'starts_at',
                'ends_at',
            ]);
        });
    }
};
