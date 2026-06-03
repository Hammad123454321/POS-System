<?php

namespace App\Modules\PlatformCore\Application\Privacy;

use App\Modules\PlatformCore\Domain\Models\Merchant;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;

class ExportMerchantData
{
    /**
     * @var array<int, string>
     */
    private const MERCHANT_SCOPED_TABLES = [
        'merchants',
        'stores',
        'device_profiles',
        'devices',
        'features',
        'dining_tables',
        'table_assignments',
        'edit_leases',
        'exception_cases',
        'tax_rules',
        'categories',
        'catalog_items',
        'price_rules',
        'register_sessions',
        'orders',
        'order_lines',
        'payments',
        'receipts',
        'cash_movements',
        'device_enrollment_codes',
        'device_refresh_tokens',
        'sync_events',
        'outbox_jobs',
        'idempotency_records',
        'membership_plans',
        'membership_ledger_entries',
        'gift_cards',
        'gift_card_ledger_entries',
        'spend_holds',
        'refunds',
        'void_records',
        'audit_logs',
        'usage_records',
        'sync_recovery_runs',
        'customers',
        'member_accounts',
        'discount_rules',
        'printer_configs',
        'print_routes',
        'payment_splits',
        'exception_resolutions',
        'feature_flags',
        'feature_flag_overrides',
        'archive_access_logs',
        'device_status_events',
        'variants',
        'modifier_groups',
        'modifier_options',
        'combo_packages',
        'combo_package_items',
        'combo_package_add_ons',
        'catalog_item_add_ons',
        'delivery_channel_configs',
        'external_order_links',
        'inventory_balances',
        'inventory_adjustments',
        'inventory_transfers',
        'receiving_records',
        'barcode_records',
        'payment_provider_events',
        'service_items',
        'staff_profiles',
        'commission_rules',
        'wage_rules',
        'staff_service_rules',
        'appointments',
        'shifts',
        'attendance_records',
        'payroll_snapshots',
    ];

    /**
     * @return array<string, mixed>
     */
    public function handle(Merchant $merchant, int $actorId): array
    {
        $recordCounts = [];
        $payload = [
            'exported_at' => Carbon::now('UTC')->toIso8601String(),
            'actor_user_id' => $actorId,
            'merchant_id' => $merchant->id,
            'tables' => [],
        ];

        foreach (self::MERCHANT_SCOPED_TABLES as $table) {
            if (! Schema::hasTable($table)) {
                continue;
            }

            $query = DB::table($table);

            if ($table === 'merchants') {
                $query->where('id', $merchant->id);
            } elseif (Schema::hasColumn($table, 'merchant_id')) {
                $query->where('merchant_id', $merchant->id);
            } else {
                continue;
            }

            $records = $query
                ->orderBy(Schema::hasColumn($table, 'created_at') ? 'created_at' : 'id')
                ->limit((int) config('pos.privacy.export_table_limit', 5000))
                ->get()
                ->map(fn (object $record): array => (array) $record)
                ->all();

            $payload['tables'][$table] = $records;
            $recordCounts[$table] = count($records);
        }

        $path = sprintf(
            'privacy-exports/merchants/%s-%s.json',
            $merchant->id,
            Str::lower(Str::random(12)),
        );

        Storage::disk('local')->put($path, json_encode($payload, JSON_PRETTY_PRINT | JSON_THROW_ON_ERROR));

        return [
            'path' => $path,
            'record_count' => $recordCounts,
            'table_count' => count($recordCounts),
        ];
    }
}
