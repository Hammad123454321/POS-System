<?php

namespace App\Modules\OrderRegister\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Domain\Models\Store;
use App\Modules\Reporting\Application\Queries\AdminOrdersQuery;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class AdminOrderController extends Controller
{
    public function __construct(
        private readonly AdminOrdersQuery $query,
    ) {}

    public function index(Request $request, Store $store): JsonResponse
    {
        Gate::authorize('viewOrders', $store);

        $validated = $request->validate([
            'status' => ['nullable', 'string', 'max:40'],
            'device_id' => ['nullable', 'string'],
            'business_date_from' => ['nullable', 'date'],
            'business_date_to' => ['nullable', 'date'],
            'q' => ['nullable', 'string', 'max:120'],
            'per_page' => ['nullable', 'integer', 'min:1', 'max:100'],
        ]);

        $paginator = $this->query->handle($store, $validated, (int) ($validated['per_page'] ?? 25));

        return response()->json([
            'data' => collect($paginator->items())->map(fn ($order) => [
                'id' => $order->id,
                'order_number' => $order->order_number,
                'status' => $order->status,
                'business_date' => $order->business_date?->format('Y-m-d'),
                'currency' => $order->currency,
                'subtotal_minor' => $order->subtotal_minor,
                'tax_minor' => $order->tax_minor,
                'discount_minor' => $order->discount_minor,
                'total_minor' => $order->total_minor,
                'paid_minor' => $order->paid_minor,
                'device_id' => $order->device_id,
                'created_at' => $order->created_at?->toIso8601String(),
            ])->all(),
            'meta' => [
                'page' => $paginator->currentPage(),
                'per_page' => $paginator->perPage(),
                'total' => $paginator->total(),
                'last_page' => $paginator->lastPage(),
            ],
        ]);
    }

    public function show(Store $store, string $order): JsonResponse
    {
        Gate::authorize('viewOrders', $store);

        $model = $this->query->find($store, $order);

        abort_if($model === null, 404);

        return response()->json([
            'data' => [
                'id' => $model->id,
                'order_number' => $model->order_number,
                'status' => $model->status,
                'business_date' => $model->business_date?->format('Y-m-d'),
                'currency' => $model->currency,
                'subtotal_minor' => $model->subtotal_minor,
                'tax_minor' => $model->tax_minor,
                'discount_minor' => $model->discount_minor,
                'tip_minor' => $model->tip_minor,
                'total_minor' => $model->total_minor,
                'paid_minor' => $model->paid_minor,
                'lines' => $model->lines->map(fn ($line) => [
                    'id' => $line->id,
                    'name' => $line->name ?? null,
                    'quantity' => $line->quantity ?? null,
                    'unit_price_minor' => $line->unit_price_minor ?? null,
                    'line_total_minor' => $line->line_total_minor ?? null,
                ])->all(),
                'payments' => $model->payments->map(fn ($payment) => [
                    'id' => $payment->id,
                    'method' => $payment->method ?? null,
                    'amount_minor' => $payment->amount_minor ?? null,
                    'status' => $payment->status ?? null,
                    'refunds' => $payment->refunds->map(fn ($r) => [
                        'id' => $r->id,
                        'amount_minor' => $r->amount_minor ?? null,
                        'reason' => $r->reason ?? null,
                    ])->all(),
                    'voids' => $payment->voidRecords->map(fn ($v) => [
                        'id' => $v->id,
                        'reason' => $v->reason ?? null,
                    ])->all(),
                ])->all(),
                'receipt' => $model->receipt ? ['id' => $model->receipt->id] : null,
            ],
        ]);
    }
}
