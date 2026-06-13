<?php

namespace App\Providers;

use App\Models\Sanctum\PersonalAccessToken;
use App\Models\User;
use App\Modules\Billing\Application\Listeners\RecordOrderPaidUsage;
use App\Modules\OrderRegister\Domain\Events\OrderPaid;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\PlatformCore\Interfaces\Authorization\MerchantPolicy;
use App\Modules\PlatformCore\Interfaces\Authorization\StorePolicy;
use App\Modules\SalonWorkforce\Application\EloquentPayrollCalculator;
use App\Modules\SalonWorkforce\Contracts\PayrollCalculator;
use App\Modules\StoredValue\Application\EloquentStoredValueLedger;
use App\Modules\StoredValue\Contracts\StoredValueLedger;
use Carbon\CarbonImmutable;
use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Date;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\Facades\Gate;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\Facades\URL;
use Illuminate\Support\ServiceProvider;
use Illuminate\Validation\Rules\Password;
use Laravel\Sanctum\Sanctum;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        $this->app->bind(StoredValueLedger::class, EloquentStoredValueLedger::class);
        $this->app->bind(PayrollCalculator::class, EloquentPayrollCalculator::class);
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        Gate::policy(Store::class, StorePolicy::class);
        Gate::policy(Merchant::class, MerchantPolicy::class);
        Gate::before(fn (User $user): ?bool => $user->is_super_admin ? true : null);

        // Module event paths are not auto-discovered — register explicitly.
        Event::listen(OrderPaid::class, RecordOrderPaidUsage::class);

        $this->configureDefaults();
    }

    /**
     * Configure default behaviors for production-ready applications.
     */
    protected function configureDefaults(): void
    {
        Date::use(CarbonImmutable::class);
        Sanctum::usePersonalAccessTokenModel(PersonalAccessToken::class);

        if (app()->environment('production')) {
            URL::forceScheme('https');
        }

        DB::prohibitDestructiveCommands(
            app()->isProduction(),
        );

        Password::defaults(fn (): ?Password => app()->isProduction()
            ? Password::min(12)
                ->mixedCase()
                ->letters()
                ->numbers()
                ->symbols()
                ->uncompromised()
            : null,
        );

        RateLimiter::for('pos-api', function (Request $request): array {
            $deviceKey = $request->user()?->id ?: $request->header('X-Device-Fingerprint') ?: $request->ip();
            $merchantKey = $request->user()?->merchant_id
                ?: $request->attributes->get('merchant_id')
                ?: 'unknown';

            return [
                Limit::perMinute(120)->by('device:'.$deviceKey),
                Limit::perMinute(3000)->by('merchant:'.$merchantKey),
            ];
        });

        RateLimiter::for('pos-sync', function (Request $request): array {
            $deviceKey = $request->user()?->id ?: $request->header('X-Device-Fingerprint') ?: $request->ip();
            $merchantKey = $request->user()?->merchant_id
                ?: $request->attributes->get('merchant_id')
                ?: 'unknown';

            return [
                Limit::perMinute(30)->by('sync-device:'.$deviceKey),
                Limit::perMinute(600)->by('sync-merchant:'.$merchantKey),
            ];
        });
    }
}
