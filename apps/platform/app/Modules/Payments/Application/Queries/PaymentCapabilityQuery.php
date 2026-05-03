<?php

namespace App\Modules\Payments\Application\Queries;

use App\Modules\Payments\Application\PaymentProviderRegistry;
use App\Modules\PlatformCore\Domain\Models\Device;

class PaymentCapabilityQuery
{
    public function __construct(
        private readonly PaymentProviderRegistry $paymentProviderRegistry,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function forDevice(Device $device): array
    {
        $provider = $this->paymentProviderRegistry->forKey();

        return [
            'default_provider' => $provider->key(),
            'terminal_capabilities' => $provider->getTerminalCapabilities($device),
            'terminal_profile' => [
                'comm_type' => (string) config('pos.payments.poslink.comm_type', 'AIDL'),
                'timeout_ms' => (int) config('pos.payments.poslink.timeout_ms', 60000),
                'profile_id' => config('pos.payments.poslink.terminal_profile.profile_id'),
                'terminal_reference' => config('pos.payments.poslink.terminal_profile.terminal_reference'),
            ],
            'supported_tenders' => ['cash', 'card', 'gift_card'],
        ];
    }
}
