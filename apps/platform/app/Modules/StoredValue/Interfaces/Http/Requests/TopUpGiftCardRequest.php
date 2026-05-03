<?php

namespace App\Modules\StoredValue\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class TopUpGiftCardRequest extends FormRequest
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
            'gift_card_code' => ['required', 'string', 'max:64'],
            'amount_minor' => ['required', 'integer', 'min:1'],
        ];
    }
}
