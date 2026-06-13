<?php

use App\Console\Commands\FlagStaleCardInDoubtCommand;
use App\Console\Commands\MonitorDlqIncidentsCommand;
use App\Console\Commands\PruneRetentionRecordsCommand;
use App\Console\Commands\ReapEditLeasesCommand;
use App\Console\Commands\ReconcileFiservPaymentsCommand;
use App\Console\Commands\RefreshReportSummariesCommand;
use Illuminate\Foundation\Inspiring;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Schedule;

Artisan::command('inspire', function () {
    $this->comment(Inspiring::quote());
})->purpose('Display an inspiring quote');

Schedule::command(ReapEditLeasesCommand::class)
    ->everyTenSeconds()
    ->withoutOverlapping();

Schedule::command(ReconcileFiservPaymentsCommand::class)
    ->hourly()
    ->withoutOverlapping();

Schedule::command(FlagStaleCardInDoubtCommand::class)
    ->everyFiveMinutes()
    ->withoutOverlapping();

Schedule::command(MonitorDlqIncidentsCommand::class)
    ->everyFiveMinutes()
    ->withoutOverlapping();

Schedule::command(PruneRetentionRecordsCommand::class)
    ->dailyAt('03:10')
    ->withoutOverlapping();

Schedule::command(RefreshReportSummariesCommand::class)
    ->everyFiveMinutes()
    ->withoutOverlapping();

Schedule::command(RefreshReportSummariesCommand::class, ['--finalize'])
    ->dailyAt('04:30')
    ->withoutOverlapping();
