<?php

namespace App\Modules\Catalog\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpsertModifierGroupRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'catalog_item_id' => ['required', 'string', Rule::exists('catalog_items', 'id')],
            'name' => ['required', 'string', 'max:255'],
            'selection_mode' => ['required', Rule::in(['single', 'multi'])],
            'min_select' => ['nullable', 'integer', 'min:0', 'max:65535'],
            'max_select' => ['nullable', 'integer', 'min:1', 'max:65535'],
            'is_required' => ['nullable', 'boolean'],
            'sort_order' => ['nullable', 'integer', 'min:0', 'max:65535'],
            'is_active' => ['nullable', 'boolean'],
        ];
    }
}
