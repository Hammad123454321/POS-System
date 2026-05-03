<?php

declare(strict_types=1);

namespace App\Platform\Support\Money;

use Brick\Math\RoundingMode;
use Brick\Money\Money;

final class MinorAmount
{
    private const BASIS_POINTS_SCALE = 10000;

    public static function calculateTax(
        int $netMinor,
        int $rateBasisPoints,
        bool $inclusive,
        string $currency,
    ): int {
        if ($rateBasisPoints <= 0 || $netMinor <= 0) {
            return 0;
        }

        $amount = self::ofMinor($netMinor, $currency);

        if ($inclusive) {
            $exclusiveBase = $amount
                ->multipliedBy(self::BASIS_POINTS_SCALE)
                ->dividedBy(self::BASIS_POINTS_SCALE + $rateBasisPoints, RoundingMode::HALF_UP);

            return self::toMinor($amount->minus($exclusiveBase));
        }

        return self::toMinor(
            $amount
                ->multipliedBy($rateBasisPoints)
                ->dividedBy(self::BASIS_POINTS_SCALE, RoundingMode::HALF_UP),
        );
    }

    public static function percentageOf(
        int $amountMinor,
        int $basisPoints,
        string $currency,
        RoundingMode $roundingMode = RoundingMode::DOWN,
    ): int {
        if ($amountMinor <= 0 || $basisPoints <= 0) {
            return 0;
        }

        return self::toMinor(
            self::ofMinor($amountMinor, $currency)
                ->multipliedBy($basisPoints)
                ->dividedBy(self::BASIS_POINTS_SCALE, $roundingMode),
        );
    }

    /**
     * @param  array<int, int>  $baseAmounts
     * @return array<int, int>
     */
    public static function allocateAcross(array $baseAmounts, int $allocationMinor): array
    {
        if ($allocationMinor <= 0 || $baseAmounts === []) {
            return array_fill(0, count($baseAmounts), 0);
        }

        $baseTotalMinor = array_sum($baseAmounts);

        if ($baseTotalMinor <= 0) {
            return array_fill(0, count($baseAmounts), 0);
        }

        $allocations = [];
        $allocatedMinor = 0;
        $lastIndex = array_key_last($baseAmounts);

        foreach ($baseAmounts as $index => $baseAmount) {
            if ($index === $lastIndex) {
                $allocations[$index] = max(0, min($allocationMinor - $allocatedMinor, $baseAmount));

                continue;
            }

            $allocation = intdiv($allocationMinor * $baseAmount, $baseTotalMinor);
            $allocations[$index] = max(0, min($allocation, $baseAmount));
            $allocatedMinor += $allocations[$index];
        }

        return array_values($allocations);
    }

    public static function ratioThreshold(int $amountMinor, int $basisPoints, string $currency): int
    {
        return self::percentageOf($amountMinor, $basisPoints, $currency, RoundingMode::HALF_UP);
    }

    private static function ofMinor(int $amountMinor, string $currency): Money
    {
        return Money::ofMinor($amountMinor, $currency);
    }

    private static function toMinor(Money $money): int
    {
        return $money->getMinorAmount()->toInt();
    }
}
