<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table): void {
            $table->boolean('is_super_admin')->default(false)->after('password');
        });

        Schema::create('user_invitations', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants')->nullOnDelete();
            $table->foreignUlid('store_id')->nullable()->constrained('stores')->nullOnDelete();
            $table->foreignUlid('role_id')->nullable()->constrained('roles')->nullOnDelete();
            $table->string('email');
            $table->string('token_hash')->unique();
            $table->foreignId('invited_by_user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->timestampTz('accepted_at')->nullable();
            $table->timestampTz('expires_at');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'email']);
            $table->index(['expires_at', 'accepted_at']);
        });

        if (DB::getDriverName() === 'pgsql') {
            DB::statement('ALTER TABLE user_invitations ENABLE ROW LEVEL SECURITY');
            DB::statement('ALTER TABLE user_invitations FORCE ROW LEVEL SECURITY');
            DB::statement('DROP POLICY IF EXISTS user_invitations_merchant_isolation ON user_invitations');
            DB::statement(
                "CREATE POLICY user_invitations_merchant_isolation ON user_invitations
                USING (merchant_id IS NULL OR merchant_id = current_setting('app.current_merchant_id', true))
                WITH CHECK (merchant_id IS NULL OR merchant_id = current_setting('app.current_merchant_id', true))"
            );
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('user_invitations');

        Schema::table('users', function (Blueprint $table): void {
            $table->dropColumn('is_super_admin');
        });
    }
};
