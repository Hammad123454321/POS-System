<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreateStoreRequest extends FormRequest
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
            'name' => ['required', 'string', 'max:160'],
            'code' => ['nullable', 'string', 'max:32'],
            'mode' => ['required', 'string', 'in:restaurant,retail,salon'],
            'timezone' => ['required', 'string', 'timezone'],
            'business_day_cutoff' => ['required', 'string', 'date_format:H:i'],
        ];
    }
}
