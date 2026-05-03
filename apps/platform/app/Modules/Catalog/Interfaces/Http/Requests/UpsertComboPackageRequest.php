<?php

namespace App\Modules\Catalog\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpsertComboPackageRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'name' => ['required', 'string', 'max:255'],
            'code' => ['nullable', 'string', 'max:255'],
            'price_minor' => ['required', 'integer', 'min:0'],
            'currency' => ['required', 'string', 'size:3'],
            'sort_order' => ['nullable', 'integer', 'min:0', 'max:65535'],
            'is_active' => ['nullable', 'boolean'],
            'items' => ['required', 'array', 'min:1'],
            'items.*.catalog_item_id' => ['required', 'string', Rule::exists('catalog_items', 'id')],
            'items.*.quantity' => ['required', 'integer', 'min:1'],
            'add_on_item_ids' => ['nullable', 'array'],
            'add_on_item_ids.*' => ['string', Rule::exists('catalog_items', 'id')],
        ];
    }
}
