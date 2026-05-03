<?php

namespace App\Modules\Payments\Interfaces\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class TenderOrderRequest extends FormRequest
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
            'tenders' => ['required', 'array', 'min:1'],
            'tenders.*.method' => ['required', Rule::in(['cash', 'card', 'gift_card'])],
            'tenders.*.amount_minor' => ['required', 'integer', 'min:1'],
            'tenders.*.tip_minor' => ['nullable', 'integer', 'min:0'],
            'tenders.*.tendered_minor' => ['nullable', 'integer', 'min:0'],
            'tenders.*.gift_card_code' => ['nullable', 'string', 'max:64'],
            'tenders.*.provider_key' => ['nullable', Rule::in(['fiserv_bluepay', 'pax_simulator'])],
            'tenders.*.provider_transaction_id' => ['nullable', 'string', 'max:64'],
            'tenders.*.auth_code' => ['nullable', 'string', 'max:32'],
            'tenders.*.masked_pan' => ['nullable', 'string', 'max:32'],
            'tenders.*.terminal_id' => ['nullable', 'string', 'max:64'],
            'tenders.*.entry_mode' => ['nullable', 'string', 'max:32'],
            'tenders.*.application_label' => ['nullable', 'string', 'max:64'],
            'tenders.*.aid' => ['nullable', 'string', 'max:64'],
            'tenders.*.tvr' => ['nullable', 'string', 'max:64'],
            'tenders.*.tsi' => ['nullable', 'string', 'max:64'],
            'tenders.*.terminal_status_code' => ['nullable', 'string', 'max:32'],
            'tenders.*.terminal_result_code' => ['nullable', 'string', 'max:32'],
            'tenders.*.terminal_timestamp' => ['nullable', 'date'],
            'tenders.*.terminal_reference' => ['nullable', 'string', 'max:120'],
        ];
    }

    public function withValidator($validator): void
    {
        $validator->after(function ($validator): void {
            $tenders = $this->input('tenders', []);

            foreach ($tenders as $index => $tender) {
                if (($tender['method'] ?? null) !== 'card') {
                    continue;
                }

                $requiredFields = [
                    'provider_transaction_id',
                    'auth_code',
                    'masked_pan',
                    'terminal_id',
                    'entry_mode',
                    'terminal_status_code',
                    'terminal_result_code',
                    'terminal_timestamp',
                ];

                foreach ($requiredFields as $field) {
                    if (! isset($tender[$field]) || trim((string) $tender[$field]) === '') {
                        $validator->errors()->add(
                            "tenders.{$index}.{$field}",
                            'Card tenders require terminal-approved result fields.',
                        );
                    }
                }
            }
        });
    }
}
