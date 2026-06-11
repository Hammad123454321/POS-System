<?php

namespace App\Modules\Catalog\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateCatalogItemRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    /**
     * @return array<string, mixed>
     */
    public function rules(): array
    {
        $store = $this->route('store');
        $merchantId = is_object($store) ? $store->merchant_id : null;
        $item = $this->route('catalogItem');
        $itemId = is_object($item) ? $item->id : null;

        return [
            'name' => ['sometimes', 'string', 'max:160'],
            'base_price_minor' => ['sometimes', 'integer', 'min:0'],
            'currency' => ['sometimes', 'string', 'size:3'],
            'category_id' => ['nullable', 'string'],
            'tax_rule_id' => ['nullable', 'string'],
            'description' => ['nullable', 'string', 'max:1000'],
            'sku' => [
                'nullable', 'string', 'max:120',
                Rule::unique('catalog_items', 'sku')
                    ->ignore($itemId)
                    ->where(fn ($q) => $q->where('merchant_id', $merchantId)),
            ],
            'sold_out' => ['nullable', 'boolean'],
        ];
    }
}
