<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('merchants', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->string('name');
            $table->char('currency', 3);
            $table->string('status')->default('active');
            $table->timestamps();
        });

        Schema::create('stores', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->string('name');
            $table->string('code')->nullable();
            $table->string('mode');
            $table->string('timezone');
            $table->string('business_day_cutoff', 5)->default('04:00');
            $table->string('status')->default('active');
            $table->timestamps();

            $table->index(['merchant_id', 'created_at']);
            $table->unique(['merchant_id', 'code']);
        });

        Schema::create('device_profiles', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->string('name');
            $table->string('type');
            $table->json('capabilities')->nullable();
            $table->timestamps();
        });

        Schema::create('devices', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->constrained('merchants');
            $table->foreignUlid('store_id')->constrained('stores');
            $table->foreignUlid('device_profile_id')->constrained('device_profiles');
            $table->string('name');
            $table->string('platform');
            $table->string('status')->default('pending');
            $table->string('device_fingerprint')->nullable();
            $table->string('drawer_code')->nullable();
            $table->timestampTz('last_seen_at')->nullable();
            $table->timestamps();

            $table->index(['merchant_id', 'store_id', 'created_at']);
            $table->unique(['store_id', 'name']);
        });

        Schema::create('roles', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->foreignUlid('merchant_id')->nullable()->constrained('merchants');
            $table->string('name');
            $table->string('scope')->default('store');
            $table->timestamps();

            $table->unique(['merchant_id', 'name']);
        });

        Schema::create('permissions', function (Blueprint $table): void {
            $table->ulid('id')->primary();
            $table->string('key')->unique();
            $table->string('description')->nullable();
            $table->timestamps();
        });

        Schema::create('permission_role', function (Blueprint $table): void {
            $table->foreignUlid('role_id')->constrained('roles')->cascadeOnDelete();
            $table->foreignUlid('permission_id')->constrained('permissions')->cascadeOnDelete();
            $table->primary(['role_id', 'permission_id']);
        });

        Schema::create('user_store_role', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->foreignUlid('store_id')->constrained('stores')->cascadeOnDelete();
            $table->foreignUlid('role_id')->constrained('roles')->cascadeOnDelete();
            $table->timestamps();

            $table->unique(['user_id', 'store_id', 'role_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('user_store_role');
        Schema::dropIfExists('permission_role');
        Schema::dropIfExists('permissions');
        Schema::dropIfExists('roles');
        Schema::dropIfExists('devices');
        Schema::dropIfExists('device_profiles');
        Schema::dropIfExists('stores');
        Schema::dropIfExists('merchants');
    }
};
