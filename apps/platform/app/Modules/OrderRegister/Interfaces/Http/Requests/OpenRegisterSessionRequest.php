<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class OpenRegisterSessionRequest extends FormRequest
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
            'opening_float_minor' => ['required', 'integer', 'min:0'],
        ];
    }
}
