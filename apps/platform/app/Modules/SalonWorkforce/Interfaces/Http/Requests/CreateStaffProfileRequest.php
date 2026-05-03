<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreateStaffProfileRequest extends FormRequest
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
            'display_name' => ['required', 'string', 'max:120'],
            'code' => ['nullable', 'string', 'max:40'],
            'role_title' => ['nullable', 'string', 'max:120'],
            'phone' => ['nullable', 'string', 'max:32'],
            'email' => ['nullable', 'email', 'max:190'],
            'hired_on' => ['nullable', 'date'],
            'metadata' => ['nullable', 'array'],
        ];
    }
}
