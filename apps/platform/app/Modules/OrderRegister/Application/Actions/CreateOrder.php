<?php

namespace App\Modules\OrderRegister\Application\Actions;

use App\Modules\Catalog\Domain\Models\CatalogItem;
use App\Modules\Catalog\Domain\Models\CatalogItemAddOn;
use App\Modules\Catalog\Domain\Models\ComboPackage;
use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\Catalog\Domain\Models\ModifierOption;
use App\Modules\Catalog\Domain\Models\Variant;
use App\Modules\CustomerValue\Domain\Models\Customer;
use App\Modules\OrderRegister\Domain\Models\Order;
use App\Modules\OrderRegister\Domain\Models\OrderLine;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\Retail\Domain\Models\BarcodeRecord;
use App\Platform\Support\Money\MinorAmount;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class CreateOrder
{
    /**
     * @param  array<int, array{
     *  catalog_item_id: string,
     *  quantity: int,
     *  variant_id?: string|null,
     *  modifier_option_ids?: array<int,string>,
     *  combo_package_id?: string|null,
     *  add_on_item_ids?: array<int,string>
     * }>  $lineInput
     */
    public function handle(
        Device $device,
        RegisterSession $registerSession,
        array $lineInput,
        ?string $customerId = null,
        ?string $discountRuleId = null,
    ): Order {
        if ($registerSession->status !== 'open') {
            throw new DomainException('Orders can only be created on an open register session.');
        }

        if ($registerSession->merchant_id !== $device->merchant_id || $registerSession->store_id !== $device->store_id) {
            throw new DomainException('The requested register session does not belong to this device context.');
        }

        return DB::transaction(function () use (
            $customerId,
            $device,
            $discountRuleId,
            $lineInput,
            $registerSession,
        ): Order {
            $merchant = $device->relationLoaded('merchant')
                ? $device->merchant
                : $device->merchant()->firstOrFail();
            $currency = (string) $merchant->currency;
            $store = $device->relationLoaded('store')
                ? $device->store
                : $device->store()->firstOrFail();

            $customer = $customerId === null ? null : Customer::query()
                ->where('merchant_id', $device->merchant_id)
                ->whereKey($customerId)
                ->where('is_active', true)
                ->with('memberAccount')
                ->firstOrFail();

            $memberAccount = $customer?->memberAccount;
            $discountRule = $discountRuleId === null ? null : DiscountRule::query()
                ->where('merchant_id', $device->merchant_id)
                ->where('is_active', true)
                ->where(function ($query) use ($device): void {
                    $query->whereNull('store_id')
                        ->orWhere('store_id', $device->store_id);
                })
                ->whereKey($discountRuleId)
                ->firstOrFail();

            $baseCatalogItemIds = array_column($lineInput, 'catalog_item_id');
            $requestedAddOnCatalogItemIds = collect($lineInput)->pluck('add_on_item_ids')->flatten()->filter()->values()->all();
            $allCatalogItemIds = array_values(array_unique([...$baseCatalogItemIds, ...$requestedAddOnCatalogItemIds]));

            $items = CatalogItem::query()
                ->whereIn('id', $allCatalogItemIds)
                ->where('merchant_id', $device->merchant_id)
                ->with(['priceRules' => fn ($query) => $query->where('is_active', true)->orderByDesc('priority')])
                ->with('taxRule')
                ->get()
                ->keyBy('id');

            $lines = [];
            $lineSubtotals = [];
            $lineCategories = [];
            $subtotalMinor = 0;
            $variantIds = collect($lineInput)->pluck('variant_id')->filter()->values()->all();
            $modifierOptionIds = collect($lineInput)->pluck('modifier_option_ids')->flatten()->filter()->values()->all();
            $comboIds = collect($lineInput)->pluck('combo_package_id')->filter()->values()->all();
            $addOnItemIds = collect($lineInput)->pluck('add_on_item_ids')->flatten()->filter()->values()->all();

            $variants = Variant::query()
                ->where('merchant_id', $device->merchant_id)
                ->whereIn('id', $variantIds)
                ->where('is_active', true)
                ->get()
                ->keyBy('id');
            $modifierOptions = ModifierOption::query()
                ->where('merchant_id', $device->merchant_id)
                ->whereIn('id', $modifierOptionIds)
                ->where('is_active', true)
                ->with('modifierGroup')
                ->get()
                ->keyBy('id');
            $combos = ComboPackage::query()
                ->where('merchant_id', $device->merchant_id)
                ->whereIn('id', $comboIds)
                ->where('is_active', true)
                ->with(['items', 'addOns'])
                ->get()
                ->keyBy('id');
            $addOnMappings = CatalogItemAddOn::query()
                ->where('merchant_id', $device->merchant_id)
                ->whereIn('add_on_item_id', $addOnItemIds)
                ->get()
                ->groupBy('catalog_item_id');

            foreach ($lineInput as $line) {
                /** @var CatalogItem|null $item */
                $item = $items->get($line['catalog_item_id']);

                if ($item === null || ! $item->is_active || $item->sold_out) {
                    throw new DomainException('One or more requested catalog items are unavailable.');
                }

                $quantity = (int) $line['quantity'];
                $unitPriceMinor = (int) ($item->priceRules->first()?->price_minor ?? $item->base_price_minor);
                $selectedVariant = null;
                $selectedModifiers = [];
                $selectedCombo = null;
                $selectedAddOns = [];

                if (! empty($line['variant_id'])) {
                    $selectedVariant = $variants->get($line['variant_id']);

                    if ($selectedVariant === null || $selectedVariant->catalog_item_id !== $item->id) {
                        throw new DomainException('Selected variant is invalid for the requested catalog item.');
                    }

                    $unitPriceMinor += (int) $selectedVariant->price_delta_minor;
                }

                $modifierDeltaMinor = 0;
                $requestedModifierOptionIds = $line['modifier_option_ids'] ?? [];

                foreach ($requestedModifierOptionIds as $modifierOptionId) {
                    $modifierOption = $modifierOptions->get($modifierOptionId);

                    if (
                        $modifierOption === null
                        || $modifierOption->modifierGroup === null
                        || $modifierOption->modifierGroup->catalog_item_id !== $item->id
                    ) {
                        throw new DomainException('One or more selected modifier options are invalid.');
                    }

                    $modifierDeltaMinor += (int) $modifierOption->price_delta_minor;
                    $selectedModifiers[] = [
                        'id' => $modifierOption->id,
                        'name' => $modifierOption->name,
                        'group_id' => $modifierOption->modifierGroup->id,
                    ];
                }

                $comboPriceMinor = 0;
                if (! empty($line['combo_package_id'])) {
                    $selectedCombo = $combos->get($line['combo_package_id']);

                    if ($selectedCombo === null) {
                        throw new DomainException('Selected combo package is invalid.');
                    }

                    $comboPriceMinor = (int) $selectedCombo->price_minor;
                }

                $addOnDeltaMinor = 0;
                $requestedAddOnIds = $line['add_on_item_ids'] ?? [];

                foreach ($requestedAddOnIds as $addOnItemId) {
                    $allowedAddOn = collect($addOnMappings->get($item->id, collect()))
                        ->first(fn (CatalogItemAddOn $mapping) => $mapping->add_on_item_id === $addOnItemId);
                    $comboAllowsAddOn = $selectedCombo !== null
                        && $selectedCombo->addOns->contains(fn ($addOn) => $addOn->catalog_item_id === $addOnItemId);

                    if ($allowedAddOn === null && ! $comboAllowsAddOn) {
                        throw new DomainException('One or more selected add-ons are invalid.');
                    }

                    $addOnItem = $items->get($addOnItemId);
                    if ($addOnItem === null) {
                        throw new DomainException('Selected add-on item is unavailable.');
                    }

                    $addOnUnitPrice = (int) ($addOnItem->priceRules->first()?->price_minor ?? $addOnItem->base_price_minor);
                    $addOnDeltaMinor += $addOnUnitPrice;
                    $selectedAddOns[] = [
                        'catalog_item_id' => $addOnItem->id,
                        'name' => $addOnItem->name,
                        'sku' => $addOnItem->sku,
                        'unit_price_minor' => $addOnUnitPrice,
                    ];
                }

                $effectiveUnitPriceMinor = $comboPriceMinor > 0
                    ? $comboPriceMinor + $modifierDeltaMinor + $addOnDeltaMinor
                    : $unitPriceMinor + $modifierDeltaMinor + $addOnDeltaMinor;
                $lineSubtotal = $effectiveUnitPriceMinor * $quantity;

                $subtotalMinor += $lineSubtotal;
                $lineSubtotals[] = $lineSubtotal;

                $lines[] = [
                    'catalog_item_id' => $item->id,
                    'name' => $item->name,
                    'sku' => $item->sku,
                    'quantity' => $quantity,
                    'unit_price_minor' => $effectiveUnitPriceMinor,
                    'subtotal_minor' => $lineSubtotal,
                    'tax_rule_snapshot' => $item->taxRule ? [
                        'id' => $item->taxRule->id,
                        'name' => $item->taxRule->name,
                        'rate_basis_points' => $item->taxRule->rate_basis_points,
                        'is_inclusive' => $item->taxRule->is_inclusive,
                    ] : null,
                    'selection_snapshot' => [
                        'variant' => $selectedVariant === null ? null : [
                            'id' => $selectedVariant->id,
                            'name' => $selectedVariant->name,
                            'code' => $selectedVariant->code,
                            'price_delta_minor' => (int) $selectedVariant->price_delta_minor,
                        ],
                        'modifiers' => $selectedModifiers,
                        'combo' => $selectedCombo === null ? null : [
                            'id' => $selectedCombo->id,
                            'name' => $selectedCombo->name,
                            'price_minor' => (int) $selectedCombo->price_minor,
                        ],
                        'add_ons' => $selectedAddOns,
                    ],
                ];
                if ($item->category_id !== null) {
                    $lineCategories[] = (string) $item->category_id;
                }
            }

            if ($discountRule === null && $store?->mode === 'retail') {
                $discountRule = $this->resolveRetailPromotion($device, $lines, $lineCategories);
            }

            $discountMinor = $this->discountMinorForRule($discountRule, $subtotalMinor, $currency);
            $allocatedDiscounts = MinorAmount::allocateAcross($lineSubtotals, $discountMinor);
            $taxMinor = 0;
            $totalMinor = 0;

            foreach ($lines as $index => &$line) {
                $lineDiscountMinor = $allocatedDiscounts[$index] ?? 0;
                $discountedSubtotal = $line['subtotal_minor'] - $lineDiscountMinor;
                $taxRule = $line['tax_rule_snapshot'];
                $lineTax = MinorAmount::calculateTax(
                    $discountedSubtotal,
                    (int) ($taxRule['rate_basis_points'] ?? 0),
                    (bool) ($taxRule['is_inclusive'] ?? false),
                    $currency,
                );
                $lineTotal = $discountedSubtotal + ((bool) ($taxRule['is_inclusive'] ?? false) ? 0 : $lineTax);

                $line['discount_minor'] = $lineDiscountMinor;
                $line['discount_snapshot'] = $discountRule === null ? null : [
                    'id' => $discountRule->id,
                    'name' => $discountRule->name,
                    'code' => $discountRule->code,
                    'scope_mode' => $discountRule->scope_mode,
                    'priority' => $discountRule->sort_order,
                    'is_stackable' => $discountRule->is_stackable,
                ];
                $line['tax_minor'] = $lineTax;
                $line['total_minor'] = $lineTotal;

                $taxMinor += $lineTax;
                $totalMinor += $lineTotal;
            }
            unset($line);

            $openedAt = CarbonImmutable::now('UTC');

            $order = Order::query()->create([
                'merchant_id' => $device->merchant_id,
                'store_id' => $device->store_id,
                'customer_id' => $customer?->id,
                'member_account_id' => $memberAccount?->status === 'active' ? $memberAccount->id : null,
                'discount_rule_id' => $discountRule?->id,
                'discount_snapshot' => $discountRule === null ? null : [
                    'id' => $discountRule->id,
                    'name' => $discountRule->name,
                    'code' => $discountRule->code,
                    'type' => $discountRule->type,
                    'value_minor' => $discountRule->value_minor,
                    'value_basis_points' => $discountRule->value_basis_points,
                ],
                'register_session_id' => $registerSession->id,
                'device_id' => $device->id,
                'order_number' => 'ORD-'.Str::upper(Str::random(10)),
                'status' => 'open',
                'business_date' => $registerSession->business_date,
                'currency' => $currency,
                'subtotal_minor' => $subtotalMinor,
                'tax_minor' => $taxMinor,
                'discount_minor' => $discountMinor,
                'total_minor' => $totalMinor,
                'paid_minor' => 0,
                'opened_at' => $openedAt,
            ]);

            foreach ($lines as $line) {
                OrderLine::query()->create([
                    'order_id' => $order->id,
                    ...$line,
                ]);
            }

            return $order->load('lines');
        });
    }

    private function discountMinorForRule(?DiscountRule $discountRule, int $subtotalMinor, string $currency): int
    {
        if ($discountRule === null || $subtotalMinor <= 0) {
            return 0;
        }

        return match ($discountRule->type) {
            'fixed_minor' => min((int) ($discountRule->value_minor ?? 0), $subtotalMinor),
            'percent_basis_points' => min(
                MinorAmount::percentageOf(
                    $subtotalMinor,
                    (int) ($discountRule->value_basis_points ?? 0),
                    $currency,
                ),
                $subtotalMinor,
            ),
            default => 0,
        };
    }

    /**
     * @param  array<int, array<string, mixed>>  $lines
     * @param  array<int, string>  $lineCategories
     */
    private function resolveRetailPromotion(Device $device, array $lines, array $lineCategories): ?DiscountRule
    {
        $now = CarbonImmutable::now('UTC');

        $candidates = DiscountRule::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('scope_mode', 'retail')
            ->where('is_active', true)
            ->where(function ($query) use ($device): void {
                $query->whereNull('store_id')
                    ->orWhere('store_id', $device->store_id);
            })
            ->where(function ($query) use ($now): void {
                $query->whereNull('starts_at')
                    ->orWhere('starts_at', '<=', $now);
            })
            ->where(function ($query) use ($now): void {
                $query->whereNull('ends_at')
                    ->orWhere('ends_at', '>=', $now);
            })
            ->orderByDesc('sort_order')
            ->orderBy('created_at')
            ->get();

        if ($candidates->isEmpty()) {
            return null;
        }

        $lineSkus = collect($lines)->pluck('sku')->filter()->map(fn ($sku) => (string) $sku)->values()->all();
        $barcodes = BarcodeRecord::query()
            ->where('merchant_id', $device->merchant_id)
            ->where('store_id', $device->store_id)
            ->whereIn('sku', $lineSkus)
            ->pluck('barcode')
            ->map(fn ($code) => (string) $code)
            ->values()
            ->all();

        foreach ($candidates as $rule) {
            $applicability = $rule->applicability ?? [];
            $ruleSkus = collect($applicability['skus'] ?? [])->map(fn ($v) => (string) $v)->values()->all();
            $ruleBarcodes = collect($applicability['barcodes'] ?? [])->map(fn ($v) => (string) $v)->values()->all();
            $ruleCategories = collect($applicability['category_ids'] ?? [])->map(fn ($v) => (string) $v)->values()->all();

            $skuMatch = $ruleSkus === [] || count(array_intersect($ruleSkus, $lineSkus)) > 0;
            $barcodeMatch = $ruleBarcodes === [] || count(array_intersect($ruleBarcodes, $barcodes)) > 0;
            $categoryMatch = $ruleCategories === [] || count(array_intersect($ruleCategories, $lineCategories)) > 0;

            if ($skuMatch && $barcodeMatch && $categoryMatch) {
                return $rule;
            }
        }

        return null;
    }
}
