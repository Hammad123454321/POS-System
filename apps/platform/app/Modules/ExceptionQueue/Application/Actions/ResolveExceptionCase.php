<?php

namespace App\Modules\ExceptionQueue\Application\Actions;

use App\Models\User;
use App\Modules\Audit\Application\AuditLogger;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionCase;
use App\Modules\ExceptionQueue\Domain\Models\ExceptionResolution;
use Carbon\CarbonImmutable;
use DomainException;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Support\Facades\DB;

class ResolveExceptionCase
{
    public function __construct(
        private readonly AuditLogger $auditLogger,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function handle(
        User $actor,
        ExceptionCase $exceptionCase,
        string $resolutionCode,
        ?string $notes = null,
    ): array {
        if ($exceptionCase->store_id === null || ! $this->canResolve($actor, $exceptionCase->store_id)) {
            throw new AuthorizationException('You are not allowed to resolve exception cases for this store.');
        }

        if ($exceptionCase->status !== 'open') {
            throw new DomainException('Only open exception cases may be resolved.');
        }

        return DB::transaction(function () use ($actor, $exceptionCase, $notes, $resolutionCode): array {
            $resolvedAt = CarbonImmutable::now('UTC');

            $resolution = ExceptionResolution::query()->create([
                'merchant_id' => $exceptionCase->merchant_id,
                'store_id' => $exceptionCase->store_id,
                'exception_case_id' => $exceptionCase->id,
                'resolved_by_user_id' => $actor->id,
                'resolution_code' => $resolutionCode,
                'notes' => $notes,
                'resolved_at' => $resolvedAt,
            ]);

            $beforeState = [
                'status' => $exceptionCase->status,
                'resolved_at' => $exceptionCase->resolved_at?->toIso8601String(),
            ];

            $exceptionCase->forceFill([
                'status' => 'resolved',
                'resolved_at' => $resolvedAt,
            ])->save();

            $this->auditLogger->log(
                $exceptionCase->merchant_id,
                $exceptionCase->store_id,
                'exception_queue',
                'exception_case.resolved',
                'exception_case',
                $exceptionCase->id,
                $beforeState,
                [
                    'status' => $exceptionCase->status,
                    'resolved_at' => $exceptionCase->resolved_at?->toIso8601String(),
                ],
                [
                    'resolution_id' => $resolution->id,
                    'resolution_code' => $resolutionCode,
                    'notes' => $notes,
                ],
                $actor->id,
            );

            return [
                'id' => $exceptionCase->id,
                'status' => $exceptionCase->status,
                'resolved_at' => $exceptionCase->resolved_at?->toIso8601String(),
                'resolution' => [
                    'id' => $resolution->id,
                    'resolution_code' => $resolution->resolution_code,
                    'notes' => $resolution->notes,
                    'resolved_by_user_id' => $resolution->resolved_by_user_id,
                    'resolved_at' => $resolution->resolved_at?->toIso8601String(),
                ],
            ];
        });
    }

    private function canResolve(User $actor, string $storeId): bool
    {
        return DB::table('user_store_role')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->where('user_store_role.user_id', $actor->id)
            ->where('user_store_role.store_id', $storeId)
            ->whereIn('roles.name', ['Merchant Owner', 'Store Admin', 'Manager'])
            ->exists();
    }
}
