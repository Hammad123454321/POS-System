<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreateEnrollmentCodeRequest extends FormRequest
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
            'device_profile_id' => ['required', 'string', 'exists:device_profiles,id'],
        ];
    }
}
