<?php

namespace App\Modules\OrderRegister\Application\Actions;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\OrderRegister\Domain\Models\CashMovement;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Platform\Support\Time\BusinessClock;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;

class OpenRegisterSession
{
    public function __construct(
        private readonly BusinessClock $businessClock,
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    public function handle(Device $device, int $openingFloatMinor): RegisterSession
    {
        $drawerCode = $device->drawer_code ?: $device->id;

        $existing = RegisterSession::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->where('drawer_code', $drawerCode)
            ->where('status', 'open')
            ->first();

        if ($existing !== null) {
            $this->openExceptionCase->handle(
                $device->merchant_id,
                $device->store_id,
                'register',
                'register_session.concurrent_open',
                'high',
                'A second device attempted to open an already-open drawer session.',
                [
                    'drawer_code' => $drawerCode,
                    'existing_register_session_id' => $existing->id,
                    'attempted_device_id' => $device->id,
                ],
                'register_session',
                $existing->id,
                $device->id,
            );

            throw new DomainException('A register session is already open for this drawer.');
        }

        return DB::transaction(function () use ($device, $drawerCode, $openingFloatMinor): RegisterSession {
            $store = $device->store()->firstOrFail();
            $openedAt = CarbonImmutable::now('UTC');

            $session = RegisterSession::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'device_id' => $device->id,
                'drawer_code' => $drawerCode,
                'business_date' => $this->businessClock->businessDateForStore($store, $openedAt),
                'status' => 'open',
                'session_version' => 1,
                'opening_float_minor' => $openingFloatMinor,
                'expected_cash_minor' => $openingFloatMinor,
                'opened_at' => $openedAt,
            ]);

            CashMovement::query()->create([
                'merchant_id' => $session->merchant_id,
                'store_id' => $session->store_id,
                'register_session_id' => $session->id,
                'device_id' => $session->device_id,
                'type' => 'float_open',
                'amount_minor' => $openingFloatMinor,
                'occurred_at' => $openedAt,
            ]);

            return $session->refresh();
        });
    }
}
