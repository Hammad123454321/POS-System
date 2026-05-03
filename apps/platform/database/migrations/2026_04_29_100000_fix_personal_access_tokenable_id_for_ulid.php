<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up(): void
    {
        if (DB::getDriverName() !== 'pgsql') {
            return;
        }

        $type = DB::table('information_schema.columns')
            ->where('table_schema', 'public')
            ->where('table_name', 'personal_access_tokens')
            ->where('column_name', 'tokenable_id')
            ->value('data_type');

        if ($type === 'bigint') {
            DB::statement('ALTER TABLE personal_access_tokens ALTER COLUMN tokenable_id TYPE varchar(36) USING tokenable_id::text');
        }
    }

    public function down(): void
    {
        // Keep as string to support ULID tokenable models.
    }
};
