<?php

namespace App\Modules\Identity\Application;

use App\Models\User;
use App\Modules\Identity\Domain\Models\UserInvitation;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class AcceptInvitation
{
    /**
     * @throws \DomainException when the token is invalid, expired, or already used
     */
    public function handle(string $token, string $name, string $password): User
    {
        $tokenHash = hash('sha256', $token);

        return DB::transaction(function () use ($tokenHash, $name, $password): User {
            /** @var UserInvitation|null $invitation */
            $invitation = UserInvitation::query()
                ->where('token_hash', $tokenHash)
                ->lockForUpdate()
                ->first();

            if ($invitation === null) {
                throw new \DomainException('Invitation not found.');
            }

            if ($invitation->accepted_at !== null) {
                throw new \DomainException('Invitation has already been accepted.');
            }

            if ($invitation->expires_at->isPast()) {
                throw new \DomainException('Invitation has expired.');
            }

            $user = User::query()->where('email', $invitation->email)->first()
                ?? User::query()->create([
                    'name' => $name,
                    'email' => $invitation->email,
                    'password' => Hash::make($password),
                    'email_verified_at' => now(),
                ]);

            if ($invitation->role_id !== null && $invitation->store_id !== null) {
                DB::table('user_store_role')->updateOrInsert(
                    [
                        'user_id' => $user->id,
                        'store_id' => $invitation->store_id,
                        'role_id' => $invitation->role_id,
                    ],
                    ['updated_at' => now(), 'created_at' => now()],
                );
            }

            $invitation->forceFill(['accepted_at' => now()])->save();

            return $user;
        });
    }
}
