<?php

namespace App\Http\Controllers;

use App\Modules\Identity\Application\AcceptInvitation;
use App\Modules\Identity\Domain\Models\UserInvitation;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;
use Inertia\Response;

class InvitationController extends Controller
{
    public function show(string $token): Response
    {
        $invitation = UserInvitation::query()
            ->where('token_hash', hash('sha256', $token))
            ->first();

        $valid = $invitation !== null && $invitation->isPending();

        return Inertia::render('auth/AcceptInvitation', [
            'token' => $token,
            'valid' => $valid,
            'email' => $valid ? $invitation->email : null,
        ]);
    }

    public function accept(Request $request, string $token, AcceptInvitation $action): RedirectResponse
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:120'],
            'password' => ['required', 'string', 'min:8', 'confirmed'],
        ]);

        try {
            $user = $action->handle($token, $validated['name'], $validated['password']);
        } catch (\DomainException $exception) {
            return back()->withErrors(['token' => $exception->getMessage()]);
        }

        Auth::login($user);

        return redirect()->intended('/dashboard');
    }
}
