<?php

namespace App\Modules\Identity\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\Identity\Application\AssignUserStoreRole;
use App\Modules\Identity\Application\InviteUser;
use App\Modules\Identity\Application\RevokeUserStoreRole;
use App\Modules\PlatformCore\Domain\Models\Store;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Gate;

class AdminStoreUserController extends Controller
{
    public function index(Store $store): JsonResponse
    {
        Gate::authorize('manageUsers', $store);

        $users = DB::table('user_store_role')
            ->join('users', 'users.id', '=', 'user_store_role.user_id')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->where('user_store_role.store_id', $store->id)
            ->get(['users.id', 'users.name', 'users.email', 'roles.id as role_id', 'roles.name as role_name'])
            ->groupBy('id')
            ->map(fn ($rows) => [
                'id' => $rows->first()->id,
                'name' => $rows->first()->name,
                'email' => $rows->first()->email,
                'roles' => $rows->map(fn ($r) => ['id' => $r->role_id, 'name' => $r->role_name])->values(),
            ])
            ->values();

        return response()->json(['data' => $users]);
    }

    public function roles(Store $store): JsonResponse
    {
        Gate::authorize('manageUsers', $store);

        $roles = DB::table('roles')
            ->where('merchant_id', $store->merchant_id)
            ->orderBy('name')
            ->get(['id', 'name']);

        return response()->json(['data' => $roles]);
    }

    public function invite(Request $request, Store $store, InviteUser $action): JsonResponse
    {
        Gate::authorize('manageUsers', $store);

        $validated = $request->validate([
            'email' => ['required', 'email', 'max:160'],
            'role_id' => ['required', 'string'],
        ]);

        /** @var User $actor */
        $actor = $request->user();

        try {
            ['invitation' => $invitation] = $action->handle($actor, $store, $validated['email'], $validated['role_id']);
        } catch (\InvalidArgumentException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }

        return response()->json(['data' => [
            'id' => $invitation->id,
            'email' => $invitation->email,
            'expires_at' => $invitation->expires_at->toIso8601String(),
        ]], 201);
    }

    public function assignRole(Request $request, Store $store, User $user, AssignUserStoreRole $action): JsonResponse
    {
        Gate::authorize('manageUsers', $store);

        $validated = $request->validate(['role_id' => ['required', 'string']]);

        try {
            $action->handle($user, $store, $validated['role_id']);
        } catch (\InvalidArgumentException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }

        return response()->json(['data' => ['user_id' => $user->id, 'role_id' => $validated['role_id']]]);
    }

    public function revokeRole(Store $store, User $user, string $role, RevokeUserStoreRole $action): JsonResponse
    {
        Gate::authorize('manageUsers', $store);

        try {
            $action->handle($user, $store, $role);
        } catch (\DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }

        return response()->json(['data' => ['user_id' => $user->id, 'revoked_role_id' => $role]]);
    }
}
