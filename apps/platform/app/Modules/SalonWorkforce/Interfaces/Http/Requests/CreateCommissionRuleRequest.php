<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Validator;

class CreateCommissionRuleRequest extends FormRequest
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
            'base_type' => ['required', 'in:service_net,service_subtotal,service_total'],
            'rate_basis_points' => ['nullable', 'integer', 'min:0', 'max:10000'],
            'fixed_minor' => ['nullable', 'integer', 'min:0'],
            'currency' => ['nullable', 'string', 'size:3'],
            'effective_from' => ['nullable', 'date'],
            'effective_to' => ['nullable', 'date'],
            'is_active' => ['nullable', 'boolean'],
        ];
    }

    public function withValidator(Validator $validator): void
    {
        $validator->after(function (Validator $validator): void {
            $hasRate = $this->filled('rate_basis_points') && (int) $this->integer('rate_basis_points') > 0;
            $hasFixed = $this->filled('fixed_minor') && (int) $this->integer('fixed_minor') > 0;

            if (! $hasRate && ! $hasFixed) {
                $validator->errors()->add('rate_basis_points', 'Either rate_basis_points or fixed_minor must be provided.');
            }
        });
    }
}
