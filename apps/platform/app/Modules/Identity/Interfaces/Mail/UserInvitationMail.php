<?php

namespace App\Modules\Identity\Interfaces\Mail;

use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class UserInvitationMail extends Mailable implements ShouldQueue
{
    use Queueable, SerializesModels;

    public function __construct(
        public readonly Store $store,
        public readonly string $token,
    ) {}

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: "You've been invited to {$this->store->name}",
        );
    }

    public function content(): Content
    {
        return new Content(
            htmlString: sprintf(
                '<p>You have been invited to join <strong>%s</strong> on the POS platform.</p>'.
                '<p><a href="%s">Accept your invitation</a></p>'.
                '<p>This link expires in 7 days.</p>',
                e($this->store->name),
                e(url("/invitations/{$this->token}")),
            ),
        );
    }
}
