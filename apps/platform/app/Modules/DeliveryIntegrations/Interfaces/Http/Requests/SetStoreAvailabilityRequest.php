<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class SetStoreAvailabilityRequest extends FormRequest
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
            'state' => ['required', Rule::in(['open', 'pause', 'resume'])],
            'reason' => ['nullable', 'string', 'max:500'],
            'channel_key' => ['nullable', Rule::in(['aggregator', 'uber_eats', 'door_dash'])],
        ];
    }
}
