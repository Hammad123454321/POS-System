<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('customers', function (Blueprint $table): void {
            $table->timestampTz('pii_tombstoned_at')->nullable()->after('is_active');
            $table->text('pii_tombstone_reason')->nullable()->after('pii_tombstoned_at');
            $table->foreignId('pii_tombstoned_by_user_id')->nullable()->after('pii_tombstone_reason')->constrained('users');
        });
    }

    public function down(): void
    {
        Schema::table('customers', function (Blueprint $table): void {
            $table->dropConstrainedForeignId('pii_tombstoned_by_user_id');
            $table->dropColumn(['pii_tombstoned_at', 'pii_tombstone_reason']);
        });
    }
};
