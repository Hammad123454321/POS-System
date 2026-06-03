<?php

use Illuminate\Support\Facades\Route;
use Inertia\Inertia;
use Laravel\Fortify\Features;

Route::inertia('/', 'Welcome', [
    'canRegister' => Features::enabled(Features::registration()),
])->name('home');

Route::middleware(['auth', 'verified'])->group(function () {
    Route::inertia('dashboard', 'Dashboard')->name('dashboard');
    Route::get('admin/{section?}', fn (?string $section = null) => Inertia::render('AdminOperations', [
        'section' => $section,
    ]))
        ->where('section', '.*')
        ->name('admin.operations');
    Route::inertia('docs/readiness', 'Readiness')->name('docs.readiness');
});

require __DIR__.'/settings.php';
