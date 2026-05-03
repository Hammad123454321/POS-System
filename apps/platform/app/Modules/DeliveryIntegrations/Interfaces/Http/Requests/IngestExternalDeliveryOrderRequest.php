<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class IngestExternalDeliveryOrderRequest extends FormRequest
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
            'channel_key' => ['required', 'in:aggregator,uber_eats,door_dash'],
            'external_order_id' => ['required', 'string', 'max:120'],
            'external_store_id' => ['nullable', 'string', 'max:120'],
            'lines' => ['nullable', 'array'],
            'lines.*.catalog_item_id' => ['nullable', 'string'],
            'lines.*.sku' => ['nullable', 'string', 'max:120'],
            'lines.*.quantity' => ['nullable', 'integer', 'min:1', 'max:1000'],
            'payload' => ['nullable', 'array'],
        ];
    }
}
