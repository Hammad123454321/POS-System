<?php

namespace App\Modules\PlatformCore\Interfaces\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Modules\PlatformCore\Application\Archive\ArchiveAccessLogger;
use App\Modules\PlatformCore\Application\Privacy\ExportMerchantData;
use App\Modules\PlatformCore\Domain\Models\Merchant;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class AdminMerchantPrivacyController extends Controller
{
    public function export(
        Request $request,
        Merchant $merchant,
        ExportMerchantData $exportMerchantData,
        ArchiveAccessLogger $archiveAccessLogger,
    ): JsonResponse {
        $request->validate([
            'reason' => ['required', 'string', 'max:500'],
        ]);

        abort_unless($this->canExportMerchant($request, $merchant), 403);

        $result = $exportMerchantData->handle($merchant, (int) $request->user()->getAuthIdentifier());

        $archiveAccessLogger->log(
            merchantId: $merchant->id,
            storeId: null,
            archiveType: 'merchant_privacy_export',
            archiveRecordId: $merchant->id,
            userId: (int) $request->user()->getAuthIdentifier(),
            deviceId: null,
            reason: $request->string('reason')->toString(),
            metadata: $result,
        );

        return response()->json(['data' => $result], 202);
    }

    private function canExportMerchant(Request $request, Merchant $merchant): bool
    {
        if ($request->user()?->is_super_admin) {
            return true;
        }

        return DB::table('user_store_role')
            ->join('roles', 'roles.id', '=', 'user_store_role.role_id')
            ->join('stores', 'stores.id', '=', 'user_store_role.store_id')
            ->where('user_store_role.user_id', $request->user()->getAuthIdentifier())
            ->where('stores.merchant_id', $merchant->id)
            ->whereIn('roles.name', ['Merchant Owner', 'Store Admin'])
            ->exists();
    }
}
