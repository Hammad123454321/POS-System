<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CashCheckoutRequest extends FormRequest
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
            'tendered_minor' => ['required', 'integer', 'min:0'],
        ];
    }
}
