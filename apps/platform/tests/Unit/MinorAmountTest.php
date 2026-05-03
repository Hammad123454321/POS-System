<?php

use App\Platform\Support\Money\MinorAmount;

it('calculates exclusive tax using minor-unit rounding without floats', function () {
    expect(MinorAmount::calculateTax(1005, 1000, false, 'USD'))->toBe(101);
});

it('calculates inclusive tax using minor-unit rounding without floats', function () {
    expect(MinorAmount::calculateTax(1075, 750, true, 'USD'))->toBe(75);
});

it('allocates discounts by minor units and assigns the remainder to the last line', function () {
    expect(MinorAmount::allocateAcross([333, 333, 334], 100))->toBe([33, 33, 34]);
});

it('calculates percentage discounts using minor-unit floor rounding', function () {
    expect(MinorAmount::percentageOf(999, 333, 'USD'))->toBe(33);
});
