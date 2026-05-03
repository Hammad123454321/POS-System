<?php

namespace App\Modules\Payments\Application;

use App\Modules\Payments\Application\Providers\FiservBluePayPaymentProvider;
use App\Modules\Payments\Application\Providers\PaxSimulatedPaymentProvider;
use App\Modules\Payments\Contracts\PaymentProvider;
use DomainException;

class PaymentProviderRegistry
{
    public function __construct(
        private readonly FiservBluePayPaymentProvider $fiservBluePayPaymentProvider,
        private readonly PaxSimulatedPaymentProvider $paxSimulatedPaymentProvider,
    ) {}

    public function forKey(?string $providerKey = null): PaymentProvider
    {
        return match ($providerKey ?? config('pos.payments.default_provider')) {
            'fiserv_bluepay' => $this->fiservBluePayPaymentProvider,
            'pax_simulator' => $this->paxSimulatedPaymentProvider,
            default => throw new DomainException('The requested payment provider is not configured.'),
        };
    }
}
