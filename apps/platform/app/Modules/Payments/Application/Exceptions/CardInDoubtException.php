<?php

namespace App\Modules\Payments\Application\Exceptions;

use DomainException;

class CardInDoubtException extends DomainException
{
    /**
     * @param  array<string, mixed>  $details
     */
    public function __construct(
        public readonly array $details = [],
        string $message = 'Card terminal response is in-doubt. Run inquiry/reconciliation before retrying this order.',
    ) {
        parent::__construct($message);
    }
}
