<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class EnrollDeviceRequest extends FormRequest
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
            'enrollment_code' => ['required', 'string'],
            'device_name' => ['required', 'string', 'max:255'],
            'platform' => ['required', 'string', Rule::in(['android'])],
            'device_fingerprint' => ['required', 'string', 'max:255'],
            'public_key' => ['required', 'string'],
            'attestation' => ['required', 'array'],
            'attestation.provider' => ['required', 'string', Rule::in(['android_keystore'])],
            'attestation.certificate_chain' => ['required', 'array', 'min:1'],
            'attestation.certificate_chain.*' => ['required', 'string'],
        ];
    }
}
