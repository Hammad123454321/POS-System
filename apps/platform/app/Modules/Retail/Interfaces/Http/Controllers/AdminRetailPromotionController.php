<?php

namespace App\Modules\Retail\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Modules\Catalog\Domain\Models\DiscountRule;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Retail\Application\Actions\SetRetailPromotionStatus;
use App\Modules\Retail\Application\Actions\UpsertRetailPromotion;
use App\Modules\Retail\Interfaces\Http\Requests\UpsertRetailPromotionRequest;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminRetailPromotionController extends Controller
{
    public function store(
        UpsertRetailPromotionRequest $request,
        Store $store,
        UpsertRetailPromotion $action,
    ): JsonResponse {
        try {
            $user = $request->user();
            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may manage retail promotions.');
            }

            return response()->json([
                'data' => $action->handle(
                    actor: $user,
                    store: $store,
                    name: $request->string('name')->toString(),
                    code: $request->filled('code') ? $request->string('code')->toString() : null,
                    type: $request->string('type')->toString(),
                    valueMinor: $request->filled('value_minor') ? (int) $request->integer('value_minor') : null,
                    valueBasisPoints: $request->filled('value_basis_points') ? (int) $request->integer('value_basis_points') : null,
                    priority: (int) $request->integer('priority', 0),
                    isStackable: $request->boolean('is_stackable'),
                    startsAt: $request->filled('starts_at') ? $request->string('starts_at')->toString() : null,
                    endsAt: $request->filled('ends_at') ? $request->string('ends_at')->toString() : null,
                    applicability: $request->input('applicability'),
                ),
            ], 201);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }

    public function update(
        UpsertRetailPromotionRequest $request,
        Store $store,
        DiscountRule $discountRule,
        UpsertRetailPromotion $action,
    ): JsonResponse {
        try {
            $user = $request->user();
            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may manage retail promotions.');
            }

            return response()->json([
                'data' => $action->handle(
                    actor: $user,
                    store: $store,
                    name: $request->string('name')->toString(),
                    code: $request->filled('code') ? $request->string('code')->toString() : null,
                    type: $request->string('type')->toString(),
                    valueMinor: $request->filled('value_minor') ? (int) $request->integer('value_minor') : null,
                    valueBasisPoints: $request->filled('value_basis_points') ? (int) $request->integer('value_basis_points') : null,
                    priority: (int) $request->integer('priority', 0),
                    isStackable: $request->boolean('is_stackable'),
                    startsAt: $request->filled('starts_at') ? $request->string('starts_at')->toString() : null,
                    endsAt: $request->filled('ends_at') ? $request->string('ends_at')->toString() : null,
                    applicability: $request->input('applicability'),
                    existing: $discountRule,
                ),
            ]);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }

    public function activate(
        Request $request,
        Store $store,
        DiscountRule $discountRule,
        SetRetailPromotionStatus $action,
    ): JsonResponse {
        return $this->setStatus($request, $store, $discountRule, true, $action);
    }

    public function deactivate(
        Request $request,
        Store $store,
        DiscountRule $discountRule,
        SetRetailPromotionStatus $action,
    ): JsonResponse {
        return $this->setStatus($request, $store, $discountRule, false, $action);
    }

    private function setStatus(
        Request $request,
        Store $store,
        DiscountRule $discountRule,
        bool $isActive,
        SetRetailPromotionStatus $action,
    ): JsonResponse {
        try {
            $user = $request->user();
            if (! $user instanceof User) {
                throw new AuthorizationException('Only authenticated back-office users may manage retail promotions.');
            }

            return response()->json([
                'data' => $action->handle($user, $store, $discountRule, $isActive),
            ]);
        } catch (AuthorizationException $exception) {
            return response()->json(['message' => $exception->getMessage()], 403);
        }
    }
}
