<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class SetItemAvailabilityRequest extends FormRequest
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
            'sold_out' => ['required', 'boolean'],
        ];
    }
}
