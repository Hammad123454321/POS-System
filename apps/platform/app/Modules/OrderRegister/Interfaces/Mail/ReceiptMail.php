<?php

namespace App\Modules\OrderRegister\Interfaces\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class ReceiptMail extends Mailable implements ShouldQueue
{
    use Queueable, SerializesModels;

    /**
     * @param  array<string, mixed>  $receiptPayload
     */
    public function __construct(
        public readonly string $receiptNumber,
        public readonly array $receiptPayload,
    ) {}

    public function envelope(): Envelope
    {
        return new Envelope(subject: "Your receipt {$this->receiptNumber}");
    }

    public function content(): Content
    {
        $lines = '';
        foreach (($this->receiptPayload['lines'] ?? []) as $line) {
            $name = e((string) ($line['name'] ?? 'Item'));
            $qty = (int) ($line['quantity'] ?? 1);
            $lines .= "<tr><td>{$name}</td><td>x{$qty}</td></tr>";
        }

        $total = (int) ($this->receiptPayload['total_minor'] ?? 0);
        $currency = e((string) ($this->receiptPayload['currency'] ?? 'USD'));

        return new Content(
            htmlString: sprintf(
                '<h2>Receipt %s</h2><table>%s</table><p><strong>Total:</strong> %s %s</p>',
                e($this->receiptNumber),
                $lines,
                number_format($total / 100, 2),
                $currency,
            ),
        );
    }
}
