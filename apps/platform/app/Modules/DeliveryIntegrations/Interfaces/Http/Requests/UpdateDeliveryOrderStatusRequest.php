<?php

namespace App\Modules\DeliveryIntegrations\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateDeliveryOrderStatusRequest extends FormRequest
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
            'status' => ['required', 'in:accepted,preparing,ready,completed,cancelled'],
        ];
    }
}
