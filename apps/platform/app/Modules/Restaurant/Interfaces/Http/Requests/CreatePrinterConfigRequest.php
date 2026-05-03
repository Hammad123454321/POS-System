<?php

namespace App\Modules\Restaurant\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreatePrinterConfigRequest extends FormRequest
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
            'driver_key' => ['required', 'string', 'max:80'],
            'connection_config' => ['nullable', 'array'],
        ];
    }
}
