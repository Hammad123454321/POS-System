<?php

namespace App\Modules\PlatformCore\Application\DeviceAuth;

class ValidateAndroidEnrollmentAttestation
{
    /**
     * @param  array<string, mixed>|null  $attestationPayload
     */
    public function handle(string $platform, string $publicKey, ?array $attestationPayload): void
    {
        if ($platform !== 'android') {
            throw new DeviceAuthException(
                'Android-only secure enrollment is enabled for POS devices.',
                422,
                'DEVICE_PLATFORM_UNSUPPORTED',
            );
        }

        if (($attestationPayload['provider'] ?? null) !== 'android_keystore') {
            throw new DeviceAuthException(
                'A valid Android keystore attestation payload is required.',
                422,
                'DEVICE_ATTESTATION_INVALID',
            );
        }

        $certificateChain = $attestationPayload['certificate_chain'] ?? null;

        if (! is_array($certificateChain) || $certificateChain === []) {
            throw new DeviceAuthException(
                'The Android attestation certificate chain is required.',
                422,
                'DEVICE_ATTESTATION_INVALID',
            );
        }

        $attestedPublicKey = $this->publicKeyFromCertificate((string) $certificateChain[0]);

        if ($attestedPublicKey === null || $this->normalizeBase64($publicKey) !== $attestedPublicKey) {
            throw new DeviceAuthException(
                'The submitted Android public key does not match the attested certificate.',
                422,
                'DEVICE_ATTESTATION_MISMATCH',
            );
        }
    }

    private function publicKeyFromCertificate(string $certificateBase64): ?string
    {
        $certificateDer = base64_decode($this->normalizeBase64($certificateBase64), true);

        if ($certificateDer === false) {
            return null;
        }

        $pemCertificate = $this->pemWrap('CERTIFICATE', $certificateDer);
        $certificate = openssl_x509_read($pemCertificate);

        if ($certificate === false) {
            return null;
        }

        $publicKey = openssl_pkey_get_public($certificate);

        if ($publicKey === false) {
            return null;
        }

        $details = openssl_pkey_get_details($publicKey);

        if (! is_array($details) || ! isset($details['key'])) {
            return null;
        }

        return $this->pemBody((string) $details['key']);
    }

    private function pemWrap(string $label, string $der): string
    {
        $body = chunk_split(base64_encode($der), 64, "\n");

        return "-----BEGIN {$label}-----\n{$body}-----END {$label}-----\n";
    }

    private function pemBody(string $pem): string
    {
        return $this->normalizeBase64(
            (string) preg_replace('/-----BEGIN [^-]+-----|-----END [^-]+-----|\s+/', '', $pem),
        );
    }

    private function normalizeBase64(string $value): string
    {
        return preg_replace('/\s+/', '', trim($value)) ?? '';
    }
}
