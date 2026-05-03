<?php

namespace App\Modules\OfflineSync\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class PushSyncEventsRequest extends FormRequest
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
            'events' => ['required', 'array', 'min:1'],
            'events.*.local_event_id' => ['required', 'string'],
            'events.*.entity_type' => ['required', 'string'],
            'events.*.entity_id' => ['nullable', 'string'],
            'events.*.action' => ['required', 'string'],
            'events.*.payload' => ['required', 'array'],
        ];
    }
}
