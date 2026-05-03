<?php

namespace App\Modules\StoredValue\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class IssueGiftCardRequest extends FormRequest
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
            'amount_minor' => ['required', 'integer', 'min:1'],
            'customer_id' => ['nullable', 'string', Rule::exists('customers', 'id')],
            'requested_code' => ['nullable', 'string', 'max:64'],
        ];
    }
}
