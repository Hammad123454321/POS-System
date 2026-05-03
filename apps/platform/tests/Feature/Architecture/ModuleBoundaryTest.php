<?php

arch('platform domain models stay inside module and platform boundaries')
    ->expect([
        'App\Modules\PlatformCore\Domain',
        'App\Modules\Identity\Domain',
        'App\Modules\Catalog\Domain',
        'App\Modules\CustomerValue\Domain',
        'App\Modules\OrderRegister\Domain',
        'App\Modules\OfflineSync\Domain',
        'App\Modules\Restaurant\Domain',
        'App\Modules\SalonWorkforce\Domain',
        'App\Modules\DeliveryIntegrations\Domain',
        'App\Modules\Retail\Domain',
        'App\Modules\ExceptionQueue\Domain',
    ])
    ->toOnlyUse([
        'App\Modules',
        'App\Platform',
        'Carbon',
        'Illuminate',
        'Laravel\Sanctum',
    ]);

arch('shared platform code is not coupled to application http controllers')
    ->expect('App\Platform')
    ->not->toUse('App\Http\Controllers');
