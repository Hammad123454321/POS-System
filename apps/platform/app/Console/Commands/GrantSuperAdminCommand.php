<?php

namespace App\Console\Commands;

use App\Models\User;
use Illuminate\Console\Command;

class GrantSuperAdminCommand extends Command
{
    protected $signature = 'app:grant-super-admin {email}';

    protected $description = 'Grant super admin access to an existing user account.';

    public function handle(): int
    {
        $email = (string) $this->argument('email');
        $user = User::query()->where('email', $email)->first();

        if (! $user instanceof User) {
            $this->error("No user found for [{$email}].");

            return self::FAILURE;
        }

        $user->forceFill(['is_super_admin' => true])->save();

        $this->info("Granted super admin access to [{$email}].");

        return self::SUCCESS;
    }
}
