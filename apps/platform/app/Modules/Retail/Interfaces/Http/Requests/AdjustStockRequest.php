<?php

namespace App\Modules\Retail\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class AdjustStockRequest extends FormRequest
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
            'sku' => ['required', 'string', 'max:120'],
            'quantity_delta' => ['required', 'integer', 'not_in:0'],
            'reason' => ['required', 'string', 'max:500'],
            'document_number' => ['nullable', 'string', 'max:120'],
            'count_session_id' => ['nullable', 'string', 'max:120'],
            'count_closed_at' => ['nullable', 'date'],
        ];
    }
}
