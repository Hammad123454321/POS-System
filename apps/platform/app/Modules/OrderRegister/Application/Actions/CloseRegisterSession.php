<?php

namespace App\Modules\OrderRegister\Application\Actions;

use App\Modules\ExceptionQueue\Application\Actions\OpenExceptionCase;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Platform\Support\Money\MinorAmount;
use Carbon\CarbonImmutable;
use DomainException;

class CloseRegisterSession
{
    public function __construct(
        private readonly OpenExceptionCase $openExceptionCase,
    ) {}

    public function handle(
        Device $device,
        RegisterSession $registerSession,
        int $countedCashMinor,
        int $sessionVersion,
    ): RegisterSession {
        if ($registerSession->status !== 'open') {
            throw new DomainException('Only open register sessions may be closed.');
        }

        if ($registerSession->device_id !== $device->id) {
            $this->openExceptionCase->handle(
                $registerSession->merchant_id,
                $registerSession->store_id,
                'register',
                'register_session.non_owner_close',
                'high',
                'A non-owner device attempted to close an open drawer session.',
                [
                    'register_session_id' => $registerSession->id,
                    'owner_device_id' => $registerSession->device_id,
                    'attempted_device_id' => $device->id,
                ],
                'register_session',
                $registerSession->id,
                $device->id,
            );

            throw new DomainException('Only the device that opened this drawer may close it.');
        }

        if ($registerSession->session_version !== $sessionVersion) {
            throw new DomainException('The register session changed on another device. Refresh and try again.');
        }

        $variance = $countedCashMinor - $registerSession->expected_cash_minor;

        $registerSession->forceFill([
            'status' => 'closed',
            'counted_cash_minor' => $countedCashMinor,
            'variance_minor' => $variance,
            'closed_at' => CarbonImmutable::now('UTC'),
            'session_version' => $registerSession->session_version + 1,
        ])->save();

        $merchant = $device->relationLoaded('merchant')
            ? $device->merchant
            : $device->merchant()->firstOrFail();
        $currency = (string) $merchant->currency;
        $threshold = max(
            (int) config('pos.exceptions.register_close_variance_minor', 2000),
            MinorAmount::ratioThreshold(
                $registerSession->expected_cash_minor,
                (int) config('pos.exceptions.register_close_variance_ratio_basis_points', 100),
                $currency,
            ),
        );

        if (abs($variance) > $threshold) {
            $this->openExceptionCase->handle(
                $registerSession->merchant_id,
                $registerSession->store_id,
                'register',
                'register_session.close_variance',
                'medium',
                'A register session closed outside the approved variance threshold.',
                [
                    'register_session_id' => $registerSession->id,
                    'expected_cash_minor' => $registerSession->expected_cash_minor,
                    'counted_cash_minor' => $countedCashMinor,
                    'variance_minor' => $variance,
                    'threshold_minor' => $threshold,
                ],
                'register_session',
                $registerSession->id,
                $device->id,
            );
        }

        return $registerSession->refresh();
    }
}
