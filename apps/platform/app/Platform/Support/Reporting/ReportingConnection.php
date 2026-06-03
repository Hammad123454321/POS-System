<?php

namespace App\Platform\Support\Reporting;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Facades\DB;
use Throwable;

class ReportingConnection
{
    public function name(): string
    {
        $connection = (string) config('pos.reporting.connection', 'pgsql_reporting');

        try {
            DB::connection($connection)->getPdo();

            return $connection;
        } catch (Throwable $exception) {
            if (! config('pos.reporting.fallback_to_primary', true)) {
                throw $exception;
            }

            return (string) config('database.default');
        }
    }

    /**
     * @param  class-string<Model>  $model
     * @return Builder<Model>
     */
    public function query(string $model): Builder
    {
        return $model::on($this->name());
    }
}
