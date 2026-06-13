<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

/**
 * Queued metering listeners can retry and double-count. A nullable source_ref
 * plus a partial unique index makes RecordUsage idempotent for event-driven
 * metering while leaving legacy direct calls (no source_ref) unconstrained.
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('usage_records', function (Blueprint $table): void {
            $table->string('source_ref')->nullable()->after('metric_key');
        });

        if (DB::getDriverName() === 'pgsql') {
            DB::statement(
                'CREATE UNIQUE INDEX IF NOT EXISTS usage_records_metric_source_unique
                 ON usage_records (metric_key, source_ref) WHERE source_ref IS NOT NULL'
            );
        }
    }

    public function down(): void
    {
        if (DB::getDriverName() === 'pgsql') {
            DB::statement('DROP INDEX IF EXISTS usage_records_metric_source_unique');
        }

        Schema::table('usage_records', function (Blueprint $table): void {
            $table->dropColumn('source_ref');
        });
    }
};
