<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('devices', function (Blueprint $table): void {
            $table->text('public_key')->nullable()->after('device_fingerprint');
            $table->json('attestation_payload')->nullable()->after('public_key');
            $table->timestampTz('enrolled_at')->nullable()->after('attestation_payload');
            $table->timestampTz('last_authenticated_at')->nullable()->after('enrolled_at');
        });

        Schema::create('device_enrollment_codes', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('device_profile_id')->constrained('device_profiles');
            $table->foreignId('created_by_user_id')->nullable()->constrained('users');
            $table->string('code_hash', 64)->unique();
            $table->timestampTz('expires_at');
            $table->timestampTz('redeemed_at')->nullable();
            $table->foreignUlid('redeemed_device_id')->nullable()->constrained('devices');
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'expires_at']);
        });

        Schema::create('device_refresh_tokens', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('device_id')->constrained('devices')->cascadeOnDelete();
            $table->string('token_family_id');
            $table->ulid('rotated_from_id')->nullable();
            $table->string('token_hash', 64)->unique();
            $table->string('device_fingerprint')->nullable();
            $table->timestampTz('expires_at');
            $table->timestampTz('last_used_at')->nullable();
            $table->timestampTz('revoked_at')->nullable();
            $table->string('revoked_reason')->nullable();
            $table->timestamps();

            $table->index(['device_id', 'token_family_id']);
            $table->index(['expires_at']);
        });

        Schema::table('device_refresh_tokens', function (Blueprint $table): void {
            $table->foreign('rotated_from_id')
                ->references('id')
                ->on('device_refresh_tokens')
                ->nullOnDelete();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('device_refresh_tokens');
        Schema::dropIfExists('device_enrollment_codes');

        Schema::table('devices', function (Blueprint $table): void {
            $table->dropColumn([
                'public_key',
                'attestation_payload',
                'enrolled_at',
                'last_authenticated_at',
            ]);
        });
    }
};
