<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CloseShiftRequest extends FormRequest
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
            'session_version' => ['required', 'integer', 'min:1'],
            'break_minutes' => ['nullable', 'integer', 'min:0', 'max:240'],
        ];
    }
}
