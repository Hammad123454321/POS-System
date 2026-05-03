<?php

namespace App\Modules\StoredValue\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class ActivateMembershipRequest extends FormRequest
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
            'customer_id' => ['required', 'string', Rule::exists('customers', 'id')],
            'membership_plan_id' => ['required', 'string', Rule::exists('membership_plans', 'id')],
            'member_number' => ['nullable', 'string', 'max:64'],
        ];
    }
}
