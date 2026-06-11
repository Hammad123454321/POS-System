<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers\SuperAdmin;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Onboarding\CreateMerchant;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class MerchantController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'q' => ['nullable', 'string', 'max:120'],
            'status' => ['nullable', Rule::in(['active', 'suspended'])],
            'per_page' => ['nullable', 'integer', 'min:1', 'max:100'],
        ]);

        $merchants = Merchant::query()
            ->when($validated['q'] ?? null, fn ($query, string $q) => $query->where('name', 'ilike', "%{$q}%"))
            ->when($validated['status'] ?? null, fn ($query, string $status) => $query->where('status', $status))
            ->orderBy('name')
            ->paginate((int) ($validated['per_page'] ?? 25));

        return response()->json($merchants);
    }

    public function store(Request $request, CreateMerchant $createMerchant): JsonResponse
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:160'],
            'currency' => ['required', 'string', 'size:3'],
        ]);

        $merchant = $createMerchant->handle($validated['name'], $validated['currency']);

        return response()->json(['data' => $merchant], 201);
    }

    public function suspend(Merchant $merchant): JsonResponse
    {
        $merchant->forceFill(['status' => 'suspended'])->save();

        return response()->json(['data' => $merchant->fresh()]);
    }

    public function reinstate(Merchant $merchant): JsonResponse
    {
        $merchant->forceFill(['status' => 'active'])->save();

        return response()->json(['data' => $merchant->fresh()]);
    }
}
