<?php

namespace App\Modules\StoredValue\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreateMembershipPlanRequest extends FormRequest
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
            'price_minor' => ['required', 'integer', 'min:1'],
            'duration_days' => ['required', 'integer', 'min:1', 'max:3660'],
            'benefits_snapshot' => ['nullable', 'array'],
        ];
    }
}
