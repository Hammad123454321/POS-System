<?php

namespace App\Modules\Identity\Application;

use App\Models\User;
use App\Modules\Identity\Domain\Models\UserInvitation;
use App\Modules\Identity\Interfaces\Mail\UserInvitationMail;
use App\Modules\PlatformCore\Domain\Models\Store;
use Carbon\CarbonImmutable;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Str;

class InviteUser
{
    /**
     * @return array{invitation: UserInvitation, token: string}
     */
    public function handle(User $actor, Store $store, string $email, string $roleId): array
    {
        // Role must belong to the store's merchant.
        $roleExists = DB::table('roles')
            ->where('id', $roleId)
            ->where('merchant_id', $store->merchant_id)
            ->exists();

        if (! $roleExists) {
            throw new \InvalidArgumentException('Role does not belong to this store\'s merchant.');
        }

        $token = Str::random(64);

        $invitation = UserInvitation::query()->create([
            'merchant_id' => $store->merchant_id,
            'store_id' => $store->id,
            'role_id' => $roleId,
            'email' => Str::lower($email),
            'token_hash' => hash('sha256', $token),
            'invited_by_user_id' => $actor->id,
            'expires_at' => CarbonImmutable::now('UTC')->addDays(7),
        ]);

        Mail::to($email)->send(new UserInvitationMail($store, $token));

        return ['invitation' => $invitation, 'token' => $token];
    }
}
