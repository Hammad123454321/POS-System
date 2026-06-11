<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Inertia\Inertia;
use Inertia\Response;

class AdminOperationsController extends Controller
{
    public function __invoke(Request $request, ?string $section = null): Response
    {
        $user = $request->user();

        abort_unless(
            $user instanceof User && ($user->is_super_admin || $user->accessibleStoreIds()->isNotEmpty()),
            403,
        );

        return Inertia::render('AdminOperations', [
            'section' => $section,
        ]);
    }
}
