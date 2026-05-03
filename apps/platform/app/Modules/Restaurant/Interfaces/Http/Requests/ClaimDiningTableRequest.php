<?php

namespace App\Modules\Restaurant\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class ClaimDiningTableRequest extends FormRequest
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
            'current_party_name' => ['nullable', 'string', 'max:120'],
            'guest_count' => ['nullable', 'integer', 'min:1', 'max:20'],
        ];
    }
}
