<?php

namespace App\Modules\ExceptionQueue\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class ResolveExceptionCaseRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    /**
     * @return array<string, array<int, mixed>>
     */
    public function rules(): array
    {
        return [
            'resolution_code' => [
                'required',
                'string',
                Rule::in(['duplicate', 'false_positive', 'resolved_in_store', 'written_off', 'recovered']),
            ],
            'notes' => ['nullable', 'string', 'max:2000'],
        ];
    }
}
