<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpsertStaffServiceRuleRequest extends FormRequest
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
            'staff_profile_id' => ['required', 'string'],
            'service_item_id' => ['required', 'string'],
            'commission_rule_id' => ['nullable', 'string'],
            'is_active' => ['nullable', 'boolean'],
        ];
    }
}
