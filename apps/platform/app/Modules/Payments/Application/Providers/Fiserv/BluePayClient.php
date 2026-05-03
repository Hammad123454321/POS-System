<?php

namespace App\Modules\Payments\Application\Providers\Fiserv;

use DomainException;
use Illuminate\Http\Client\Response;
use Illuminate\Support\Facades\Http;

class BluePayClient
{
    public function __construct(
        private readonly BluePayCrypto $bluePayCrypto,
    ) {}

    /**
     * @param  array<string, string>  $requestFields
     * @return array<string, string>
     */
    public function postTransaction(array $requestFields): array
    {
        $config = $this->config();
        $accountId = $this->requiredConfig('account_id');
        $secretKey = $this->requiredConfig('secret_key');
        $hashType = strtoupper((string) ($requestFields['TPS_HASH_TYPE'] ?? $config['hash_type'] ?? 'HMAC_SHA256'));
        $tpsDef = (string) ($requestFields['TPS_DEF'] ?? 'ACCOUNT_ID TRANS_TYPE AMOUNT MASTER_ID NAME1 PAYMENT_ACCOUNT');
        $payload = [
            'ACCOUNT_ID' => $accountId,
            'MODE' => (string) ($requestFields['MODE'] ?? $config['mode'] ?? 'TEST'),
            'TPS_HASH_TYPE' => $hashType,
            ...$requestFields,
        ];

        if (($config['user_id'] ?? null) !== null && ($payload['USER_ID'] ?? '') === '') {
            $payload['USER_ID'] = (string) $config['user_id'];
        }

        $payload['TAMPER_PROOF_SEAL'] = $this->bluePayCrypto->createSeal(
            $secretKey,
            $hashType,
            $tpsDef,
            $payload,
        );

        $response = Http::asForm()
            ->accept('text/plain')
            ->timeout(20)
            ->post((string) $config['bp20post_url'], $payload);

        $parsed = $this->parseUrlEncodedBody($response);
        $this->verifyResponseStamp($parsed, $secretKey, $hashType);

        return $parsed;
    }

    /**
     * @param  array<string, string>  $filters
     * @return array<int, array<string, string>>
     */
    public function fetchDailyReport(array $filters): array
    {
        $config = $this->config();
        $accountId = $this->requiredConfig('account_id');
        $secretKey = $this->requiredConfig('secret_key');
        $hashType = strtoupper((string) ($filters['TPS_HASH_TYPE'] ?? $config['hash_type'] ?? 'HMAC_SHA256'));
        $reportStartDate = (string) ($filters['REPORT_START_DATE'] ?? '');
        $reportEndDate = (string) ($filters['REPORT_END_DATE'] ?? '');

        if ($reportStartDate === '' || $reportEndDate === '') {
            throw new DomainException('Daily report requests require a report start and end date.');
        }

        $payload = [
            'ACCOUNT_ID' => $accountId,
            'MODE' => (string) ($filters['MODE'] ?? $config['mode'] ?? 'TEST'),
            'TPS_HASH_TYPE' => $hashType,
            'RESPONSEVERSION' => (string) ($filters['RESPONSEVERSION'] ?? '99'),
            ...$filters,
        ];
        $tpsDef = (string) ($payload['TPS_DEF'] ?? 'ACCOUNT_ID PLATFORM_MERCHANT_ID REPORT_START_DATE REPORT_END_DATE');
        $payload['TAMPER_PROOF_SEAL'] = $this->bluePayCrypto->createSeal(
            $secretKey,
            $hashType,
            $tpsDef,
            $payload,
        );

        $response = Http::asForm()
            ->accept('text/csv')
            ->timeout(30)
            ->post((string) $config['daily_report_url'], $payload);

        if ($response->failed()) {
            throw new DomainException(
                'Daily report request failed: '.trim($response->body()),
            );
        }

        return $this->parseCsvRows($response->body());
    }

    /**
     * @return array<string, mixed>
     */
    public function config(): array
    {
        return config('pos.payments.fiserv_bluepay', []);
    }

    private function requiredConfig(string $key): string
    {
        $value = $this->config()[$key] ?? null;

        if (! is_string($value) || trim($value) === '') {
            throw new DomainException('Missing required Fiserv BluePay configuration: '.$key.'.');
        }

        return trim($value);
    }

    /**
     * @return array<string, string>
     */
    private function parseUrlEncodedBody(Response $response): array
    {
        if (! $response->ok()) {
            throw new DomainException(
                'Gateway request failed with status '.$response->status().'.',
            );
        }

        $rawBody = trim($response->body());

        if ($rawBody === '') {
            throw new DomainException('Gateway response was empty.');
        }

        parse_str($rawBody, $parsed);

        if ($parsed === []) {
            throw new DomainException('Gateway response could not be parsed.');
        }

        $normalized = [];

        foreach ($parsed as $key => $value) {
            if (! is_string($key) || is_array($value)) {
                continue;
            }

            $normalized[strtoupper($key)] = (string) $value;
        }

        return $normalized;
    }

    /**
     * @param  array<string, string>  $response
     */
    private function verifyResponseStamp(array $response, string $secretKey, string $fallbackHashType): void
    {
        if (($response['BP_STAMP'] ?? '') === '') {
            return;
        }

        $definition = (string) ($response['BP_STAMP_DEF'] ?? 'TRANS_ID ISSUE_DATE STATUS');
        $hashType = (string) ($response['TPS_HASH_TYPE'] ?? $fallbackHashType);
        $isValid = $this->bluePayCrypto->verifyStamp(
            $secretKey,
            $hashType,
            $definition,
            $response,
            (string) $response['BP_STAMP'],
        );

        if (! $isValid) {
            throw new DomainException('Gateway BP_STAMP verification failed.');
        }
    }

    /**
     * @return array<int, array<string, string>>
     */
    private function parseCsvRows(string $body): array
    {
        $trimmed = trim($body);

        if ($trimmed === '') {
            return [];
        }

        $lines = preg_split('/\r\n|\n|\r/', $trimmed) ?: [];
        $rows = array_values(array_filter($lines, fn (string $line): bool => trim($line) !== ''));

        if (count($rows) < 2) {
            return [];
        }

        $headers = array_map(
            fn (string $header): string => strtoupper(trim($header)),
            str_getcsv($rows[0]),
        );
        $records = [];

        for ($index = 1; $index < count($rows); $index++) {
            $columns = str_getcsv($rows[$index]);

            if ($columns === [null]) {
                continue;
            }

            $record = [];

            foreach ($headers as $columnIndex => $header) {
                $record[$header] = (string) ($columns[$columnIndex] ?? '');
            }

            $records[] = $record;
        }

        return $records;
    }
}
