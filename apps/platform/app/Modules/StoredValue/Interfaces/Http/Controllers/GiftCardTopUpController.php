<?php

namespace App\Modules\StoredValue\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Device;
use App\Modules\StoredValue\Application\Actions\TopUpGiftCard;
use App\Modules\StoredValue\Interfaces\Http\Requests\TopUpGiftCardRequest;
use DomainException;
use Illuminate\Http\JsonResponse;

class GiftCardTopUpController extends Controller
{
    public function __invoke(TopUpGiftCardRequest $request, TopUpGiftCard $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();

            return response()->json([
                'data' => $action->handle(
                    $device,
                    $request->string('gift_card_code')->toString(),
                    (int) $request->integer('amount_minor'),
                ),
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
