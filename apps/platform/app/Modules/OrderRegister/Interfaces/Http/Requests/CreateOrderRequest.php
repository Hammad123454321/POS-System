<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class CreateOrderRequest extends FormRequest
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
            'register_session_id' => ['required', 'string', Rule::exists('register_sessions', 'id')],
            'customer_id' => ['nullable', 'string', Rule::exists('customers', 'id')],
            'discount_rule_id' => ['nullable', 'string', Rule::exists('discount_rules', 'id')],
            'lines' => ['required', 'array', 'min:1'],
            'lines.*.catalog_item_id' => ['required', 'string', Rule::exists('catalog_items', 'id')],
            'lines.*.quantity' => ['required', 'integer', 'min:1'],
            'lines.*.variant_id' => ['nullable', 'string', Rule::exists('variants', 'id')],
            'lines.*.modifier_option_ids' => ['nullable', 'array'],
            'lines.*.modifier_option_ids.*' => ['string', Rule::exists('modifier_options', 'id')],
            'lines.*.combo_package_id' => ['nullable', 'string', Rule::exists('combo_packages', 'id')],
            'lines.*.add_on_item_ids' => ['nullable', 'array'],
            'lines.*.add_on_item_ids.*' => ['string', Rule::exists('catalog_items', 'id')],
        ];
    }
}
