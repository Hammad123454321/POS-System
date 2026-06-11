<?php

use App\Http\Controllers\AdminOperationsController;
use App\Http\Controllers\SuperAdminOperationsController;
use Illuminate\Support\Facades\Route;
use Laravel\Fortify\Features;

Route::inertia('/', 'Welcome', [
    'canRegister' => Features::enabled(Features::registration()),
])->name('home');

Route::middleware(['auth', 'verified'])->group(function () {
    Route::inertia('dashboard', 'Dashboard')->name('dashboard');
    Route::get('admin/{section?}', AdminOperationsController::class)
        ->where('section', '.*')
        ->name('admin.operations');
    Route::get('super-admin/{section?}', SuperAdminOperationsController::class)
        ->where('section', '.*')
        ->name('super_admin.operations');
    Route::inertia('docs/readiness', 'Readiness')->name('docs.readiness');
});

require __DIR__.'/settings.php';
