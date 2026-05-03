<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpsertDeliveryChannelConfigRequest extends FormRequest
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
            'is_enabled' => ['required', 'boolean'],
            'credentials' => ['nullable', 'array'],
            'mapping' => ['nullable', 'array'],
            'pause_windows' => ['nullable', 'array'],
            'pause_windows.*.start_at' => ['required_with:pause_windows', 'date'],
            'pause_windows.*.end_at' => ['required_with:pause_windows', 'date'],
            'default_prep_time_minutes' => ['nullable', 'integer', 'min:1', 'max:180'],
            'sync_hours_enabled' => ['nullable', 'boolean'],
            'sync_prep_time_enabled' => ['nullable', 'boolean'],
            'sync_menu_enabled' => ['nullable', 'boolean'],
        ];
    }
}
