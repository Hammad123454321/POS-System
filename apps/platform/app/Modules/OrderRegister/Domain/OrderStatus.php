<?php

namespace App\Modules\OrderRegister\Domain;

/**
 * Legal order status transitions used by sync arbitration. A transition is legal
 * only if the target appears in the source's allowed set.
 */
final class OrderStatus
{
    public const OPEN = 'open';

    public const SENT = 'sent';

    public const PAID = 'paid';

    public const VOIDED = 'voided';

    /**
     * @var array<string, array<int, string>>
     */
    public const TRANSITIONS = [
        self::OPEN => [self::SENT, self::PAID, self::VOIDED],
        self::SENT => [self::PAID, self::VOIDED],
        self::PAID => [],
        self::VOIDED => [],
    ];

    /**
     * Money-bearing target states require manual review on conflict.
     *
     * @var array<int, string>
     */
    public const MONEY_BEARING = [self::PAID];

    public static function canTransition(string $from, string $to): bool
    {
        return in_array($to, self::TRANSITIONS[$from] ?? [], true);
    }

    public static function isMoneyBearing(string $status): bool
    {
        return in_array($status, self::MONEY_BEARING, true);
    }
}
