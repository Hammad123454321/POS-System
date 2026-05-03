<?php

namespace App\Modules\StoredValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Application\Actions\IssueGiftCard;
use App\Modules\StoredValue\Interfaces\Http\Requests\IssueGiftCardRequest;
use DomainException;
use Illuminate\Http\JsonResponse;

class GiftCardIssueController extends Controller
{
    public function __invoke(IssueGiftCardRequest $request, IssueGiftCard $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    $device,
                    (int) $request->integer('amount_minor'),
                    $request->filled('customer_id') ? $request->string('customer_id')->toString() : null,
                    $request->filled('requested_code') ? $request->string('requested_code')->toString() : null,
                ),
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
