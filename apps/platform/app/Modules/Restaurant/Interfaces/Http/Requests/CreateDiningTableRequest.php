<?php

namespace App\Modules\Restaurant\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreateDiningTableRequest extends FormRequest
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
            'zone_name' => ['nullable', 'string', 'max:120'],
            'capacity' => ['required', 'integer', 'min:1', 'max:20'],
            'sort_order' => ['nullable', 'integer', 'min:0', 'max:999'],
        ];
    }
}
