<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CancelDeliveryOrderRequest extends FormRequest
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
            'reason' => ['nullable', 'string', 'max:500'],
        ];
    }
}
