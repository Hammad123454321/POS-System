<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreateDeviceStatusEventRequest extends FormRequest
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
            'status' => ['required', 'string', 'max:120'],
            'metadata' => ['nullable', 'array'],
            'occurred_at' => ['nullable', 'date'],
        ];
    }
}
