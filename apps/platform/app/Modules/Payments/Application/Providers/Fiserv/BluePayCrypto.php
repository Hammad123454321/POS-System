<?php

namespace App\Modules\Payments\Application\Providers\Fiserv;

use DomainException;

class BluePayCrypto
{
    /**
     * @param  array<string, string>  $fields
     */
    public function createSeal(
        string $secretKey,
        string $hashType,
        string $definition,
        array $fields,
    ): string {
        $message = $this->buildMessage($definition, $fields);

        return $this->hash($secretKey, $message, $hashType);
    }

    /**
     * @param  array<string, string>  $fields
     */
    public function verifyStamp(
        string $secretKey,
        string $hashType,
        string $definition,
        array $fields,
        string $providedStamp,
    ): bool {
        $expected = $this->createSeal($secretKey, $hashType, $definition, $fields);

        return hash_equals(strtolower($expected), strtolower($providedStamp));
    }

    /**
     * @param  array<string, string>  $fields
     */
    private function buildMessage(string $definition, array $fields): string
    {
        $parts = preg_split('/\s+/', trim($definition)) ?: [];
        $message = '';

        foreach ($parts as $part) {
            if ($part === '') {
                continue;
            }

            $message .= (string) ($fields[$part] ?? '');
        }

        return $message;
    }

    private function hash(string $secretKey, string $message, string $hashType): string
    {
        return match (strtoupper($hashType)) {
            'MD5' => md5($secretKey.$message),
            'SHA256' => hash('sha256', $secretKey.$message),
            'SHA512' => hash('sha512', $secretKey.$message),
            'HMAC_SHA256' => hash_hmac('sha256', $message, $secretKey),
            'HMAC_SHA512' => hash_hmac('sha512', $message, $secretKey),
            default => throw new DomainException('Unsupported Fiserv hash type.'),
        };
    }
}
