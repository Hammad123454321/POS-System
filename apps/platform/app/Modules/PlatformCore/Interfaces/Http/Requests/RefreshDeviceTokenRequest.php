<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class RefreshDeviceTokenRequest extends FormRequest
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
            'refresh_token' => ['required', 'string'],
        ];
    }
}
