<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreateServiceItemRequest extends FormRequest
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
            'sku' => ['nullable', 'string', 'max:80'],
            'base_price_minor' => ['required', 'integer', 'min:0'],
            'duration_minutes' => ['required', 'integer', 'min:5', 'max:600'],
            'buffer_minutes' => ['nullable', 'integer', 'min:0', 'max:120'],
            'is_walk_in_enabled' => ['nullable', 'boolean'],
        ];
    }
}
