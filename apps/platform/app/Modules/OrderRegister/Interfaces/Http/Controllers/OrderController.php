<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\OrderRegister\Application\Actions\CreateOrder;
use App\Modules\OrderRegister\Domain\Models\RegisterSession;
use App\Modules\OrderRegister\Interfaces\Http\Requests\CreateOrderRequest;
use App\Modules\PlatformCore\Domain\Models\Device;
use DomainException;
use Illuminate\Http\JsonResponse;

class OrderController extends Controller
{
    public function store(CreateOrderRequest $request, CreateOrder $action): JsonResponse
    {
        try {
            /** @var Device $device */
            $device = $request->user();
            $registerSession = RegisterSession::query()->findOrFail($request->string('register_session_id')->toString());

            $order = $action->handle(
                $device,
                $registerSession,
                $request->collect('lines')
                    ->map(fn (array $line): array => [
                        'catalog_item_id' => (string) $line['catalog_item_id'],
                        'quantity' => (int) $line['quantity'],
                        'variant_id' => isset($line['variant_id']) ? (string) $line['variant_id'] : null,
                        'modifier_option_ids' => collect($line['modifier_option_ids'] ?? [])->map(fn ($id) => (string) $id)->values()->all(),
                        'combo_package_id' => isset($line['combo_package_id']) ? (string) $line['combo_package_id'] : null,
                        'add_on_item_ids' => collect($line['add_on_item_ids'] ?? [])->map(fn ($id) => (string) $id)->values()->all(),
                    ])->values()->all(),
                $request->filled('customer_id') ? $request->string('customer_id')->toString() : null,
                $request->filled('discount_rule_id') ? $request->string('discount_rule_id')->toString() : null,
            );

            return response()->json([
                'data' => [
                    'id' => $order->id,
                    'order_number' => $order->order_number,
                    'status' => $order->status,
                    'customer_id' => $order->customer_id,
                    'discount_minor' => $order->discount_minor,
                    'subtotal_minor' => $order->subtotal_minor,
                    'tax_minor' => $order->tax_minor,
                    'total_minor' => $order->total_minor,
                    'line_count' => $order->lines->count(),
                ],
            ], 201);
        } catch (DomainException $exception) {
            return response()->json(['message' => $exception->getMessage()], 422);
        }
    }
}
