<?php

namespace App\Modules\SalonWorkforce\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreateAppointmentRequest extends FormRequest
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
            'slot_claim_id' => ['required', 'string'],
            'staff_profile_id' => ['required', 'string'],
            'service_item_id' => ['required', 'string'],
            'customer_id' => ['nullable', 'string'],
            'starts_at' => ['required', 'date'],
            'ends_at' => ['required', 'date'],
            'source' => ['nullable', 'in:walk_in,online,phone'],
            'discount_minor' => ['nullable', 'integer', 'min:0'],
            'notes' => ['nullable', 'string', 'max:1000'],
        ];
    }
}
