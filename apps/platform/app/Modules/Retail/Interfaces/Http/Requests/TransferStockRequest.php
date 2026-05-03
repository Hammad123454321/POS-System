<?php

namespace App\Modules\Retail\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class TransferStockRequest extends FormRequest
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
            'document_number' => ['required', 'string', 'max:120'],
            'destination_store_id' => ['required', 'string'],
            'reason' => ['nullable', 'string', 'max:500'],
            'lines' => ['required', 'array', 'min:1'],
            'lines.*.sku' => ['required', 'string', 'max:120'],
            'lines.*.quantity' => ['required', 'integer', 'min:1', 'max:100000'],
        ];
    }
}
