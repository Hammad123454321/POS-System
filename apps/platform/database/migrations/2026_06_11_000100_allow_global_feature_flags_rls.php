<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

/**
 * Platform-wide feature flags are stored with merchant_id = NULL. The original
 * schema made merchant_id NOT NULL and the RLS policy only admitted rows whose
 * merchant_id matched the current tenant — both blocking global flags. Drop the
 * NOT NULL constraint, add a partial unique index for global rows (Postgres
 * treats NULLs as distinct in composite uniques), and broaden the RLS policy.
 */
return new class extends Migration
{
    public function up(): void
    {
        if (DB::getDriverName() !== 'pgsql') {
            return;
        }

        DB::statement('ALTER TABLE feature_flags ALTER COLUMN merchant_id DROP NOT NULL');

        DB::statement(
            'CREATE UNIQUE INDEX IF NOT EXISTS feature_flags_global_flag_key_unique
             ON feature_flags (flag_key) WHERE merchant_id IS NULL'
        );

        DB::statement('DROP POLICY IF EXISTS feature_flags_merchant_isolation ON feature_flags');
        DB::statement(
            "CREATE POLICY feature_flags_merchant_isolation ON feature_flags
            USING (merchant_id IS NULL OR merchant_id = current_setting('app.current_merchant_id', true))
            WITH CHECK (merchant_id IS NULL OR merchant_id = current_setting('app.current_merchant_id', true))"
        );
    }

    public function down(): void
    {
        if (DB::getDriverName() !== 'pgsql') {
            return;
        }

        DB::statement('DELETE FROM feature_flags WHERE merchant_id IS NULL');
        DB::statement('DROP INDEX IF EXISTS feature_flags_global_flag_key_unique');
        DB::statement('DROP POLICY IF EXISTS feature_flags_merchant_isolation ON feature_flags');
        DB::statement(
            "CREATE POLICY feature_flags_merchant_isolation ON feature_flags
            USING ((merchant_id)::text = current_setting('app.current_merchant_id', true))
            WITH CHECK ((merchant_id)::text = current_setting('app.current_merchant_id', true))"
        );
        DB::statement('ALTER TABLE feature_flags ALTER COLUMN merchant_id SET NOT NULL');
    }
};
