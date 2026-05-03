<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CloseRegisterSessionRequest extends FormRequest
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
            'counted_cash_minor' => ['required', 'integer', 'min:0'],
            'session_version' => ['required', 'integer', 'min:1'],
        ];
    }
}
