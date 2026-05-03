<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpsertFeatureFlagRequest extends FormRequest
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
            'scope' => ['required', 'string', Rule::in(['merchant', 'store'])],
            'enabled' => ['required', 'bool'],
            'value' => ['nullable'],
        ];
    }
}
