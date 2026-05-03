<?php

namespace App\Modules\Restaurant\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class CreatePrintRouteRequest extends FormRequest
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
            'route_key' => ['required', 'string', 'max:120'],
            'document_type' => ['required', Rule::in(['receipt'])],
            'primary_printer_config_id' => ['required', 'string', Rule::exists('printer_configs', 'id')],
            'secondary_printer_config_id' => ['nullable', 'string', Rule::exists('printer_configs', 'id')],
        ];
    }
}
