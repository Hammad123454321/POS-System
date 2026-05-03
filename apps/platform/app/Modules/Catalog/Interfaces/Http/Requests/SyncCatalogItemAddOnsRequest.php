<?php

namespace App\Modules\Catalog\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class SyncCatalogItemAddOnsRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'add_on_item_ids' => ['required', 'array'],
            'add_on_item_ids.*' => ['string', Rule::exists('catalog_items', 'id')],
        ];
    }
}
