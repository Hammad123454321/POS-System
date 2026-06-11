<?php

namespace App\Modules\Catalog\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StoreCatalogItemRequest extends FormRequest
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

        return [
            'name' => ['required', 'string', 'max:160'],
            'type' => ['required', 'string', 'in:product,service'],
            'base_price_minor' => ['required', 'integer', 'min:0'],
            'currency' => ['required', 'string', 'size:3'],
            'category_id' => ['nullable', 'string'],
            'tax_rule_id' => ['nullable', 'string'],
            'description' => ['nullable', 'string', 'max:1000'],
            'sku' => [
                'nullable', 'string', 'max:120',
                Rule::unique('catalog_items', 'sku')->where(fn ($q) => $q->where('merchant_id', $merchantId)),
            ],
            'sold_out' => ['nullable', 'boolean'],
        ];
    }
}
