<?php

namespace App\Modules\Catalog\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class CreateDiscountRuleRequest extends FormRequest
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
        return [
            'name' => ['required', 'string', 'max:120'],
            'code' => ['nullable', 'string', 'max:40'],
            'type' => ['required', Rule::in(['fixed_minor', 'percent_basis_points'])],
            'value_minor' => ['nullable', 'integer', 'min:1'],
            'value_basis_points' => ['nullable', 'integer', 'min:1', 'max:10000'],
            'sort_order' => ['nullable', 'integer', 'min:0', 'max:65535'],
            'scope_mode' => ['nullable', Rule::in(['all', 'retail'])],
            'applicability' => ['nullable', 'array'],
            'applicability.skus' => ['nullable', 'array'],
            'applicability.skus.*' => ['string', 'max:120'],
            'applicability.barcodes' => ['nullable', 'array'],
            'applicability.barcodes.*' => ['string', 'max:120'],
            'applicability.category_ids' => ['nullable', 'array'],
            'applicability.category_ids.*' => ['string'],
            'is_stackable' => ['nullable', 'boolean'],
            'starts_at' => ['nullable', 'date'],
            'ends_at' => ['nullable', 'date', 'after_or_equal:starts_at'],
        ];
    }
}
