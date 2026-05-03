<?php

use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\DB;

it('runs fresh postgres migrations with device refresh token self reference intact', function () {
    $exit = Artisan::call('migrate:fresh', [
        '--env' => 'testing',
        '--force' => true,
    ]);

    expect($exit)->toBe(0);
    expect(DB::getSchemaBuilder()->hasTable('device_refresh_tokens'))->toBeTrue();
    expect(DB::getSchemaBuilder()->hasTable('personal_access_tokens'))->toBeTrue();
});
