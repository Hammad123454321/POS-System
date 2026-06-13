<?php

return [
    'api' => [
        'current_major' => (int) env('POS_CURRENT_API_MAJOR', 1),
        'supported_majors' => array_map(
            'intval',
            explode(',', env('POS_SUPPORTED_API_MAJORS', '1')),
        ),
        'min_supported_major' => (int) env('POS_MIN_SUPPORTED_MAJOR', 1),
        'min_supported_app_version' => env('POS_MIN_SUPPORTED_APP_VERSION', '0.1.0'),
        'sunset_at' => env('POS_SUNSET_AT'),
    ],

    'auth' => [
        'access_token_ttl_minutes' => (int) env('POS_ACCESS_TOKEN_TTL_MINUTES', 15),
        'refresh_token_ttl_days' => (int) env('POS_REFRESH_TOKEN_TTL_DAYS', 30),
        'enrollment_ttl_minutes' => (int) env('POS_ENROLLMENT_TTL_MINUTES', 15),
        'silent_refresh_window_minutes' => (int) env('POS_SILENT_REFRESH_WINDOW_MINUTES', 5),
    ],

    'idempotency' => [
        'ttl_hours' => 72,
    ],

    'sync' => [
        'delta_page_size' => 100,
        'recovery_batch_size' => 50,
        'recovery_connection' => env('POS_SYNC_RECOVERY_QUEUE_CONNECTION', env('QUEUE_CONNECTION', 'database')),
        'recovery_queue' => env('POS_SYNC_RECOVERY_QUEUE', 'sync'),
    ],

    'delivery' => [
        'queue_connection' => env('POS_DELIVERY_QUEUE_CONNECTION', env('QUEUE_CONNECTION', 'sync')),
        'queue' => env('POS_DELIVERY_QUEUE', 'delivery'),
    ],

    'delivery_channels' => [
        'sandbox' => (bool) env('POS_DELIVERY_SANDBOX', true),
        'timeout_seconds' => (int) env('POS_DELIVERY_TIMEOUT_SECONDS', 10),
        'connect_timeout_seconds' => (int) env('POS_DELIVERY_CONNECT_TIMEOUT_SECONDS', 3),
        'retries' => (int) env('POS_DELIVERY_RETRIES', 3),
        'retry_backoff_ms' => (int) env('POS_DELIVERY_RETRY_BACKOFF_MS', 250),
        'uber_eats' => [
            'base_url' => env('UBER_EATS_BASE_URL'),
            'sandbox_base_url' => env('UBER_EATS_SANDBOX_URL'),
        ],
        'door_dash' => [
            'base_url' => env('DOOR_DASH_BASE_URL'),
            'sandbox_base_url' => env('DOOR_DASH_SANDBOX_URL'),
        ],
    ],

    'payments' => [
        'default_provider' => env('POS_DEFAULT_PAYMENT_PROVIDER', 'fiserv_bluepay'),
        'terminal_tip_adjust_supported' => false,
        'in_doubt_stale_minutes' => (int) env('POS_IN_DOUBT_STALE_MINUTES', 15),
        'poslink' => [
            'comm_type' => env('POS_PAX_POSLINK_COMM_TYPE', 'AIDL'),
            'timeout_ms' => (int) env('POS_PAX_POSLINK_TIMEOUT_MS', 60000),
            'terminal_profile' => [
                'profile_id' => env('POS_PAX_TERMINAL_PROFILE_ID'),
                'terminal_reference' => env('POS_PAX_TERMINAL_REFERENCE'),
            ],
        ],
        'fiserv_bluepay' => [
            'account_id' => env('FISERV_BLUEPAY_ACCOUNT_ID'),
            'user_id' => env('FISERV_BLUEPAY_USER_ID'),
            'secret_key' => env('FISERV_BLUEPAY_SECRET_KEY'),
            'hash_type' => env('FISERV_BLUEPAY_HASH_TYPE', 'HMAC_SHA256'),
            'mode' => env('FISERV_BLUEPAY_MODE', 'TEST'),
            'bp20post_url' => env('FISERV_BLUEPAY_BP20POST_URL', 'https://secure.bluepay.com/interfaces/bp20post'),
            'daily_report_url' => env('FISERV_BLUEPAY_DAILY_REPORT_URL', 'https://secure.bluepay.com/interfaces/bpdailyreport2'),
            'webhook' => [
                'verify_bp_stamp' => filter_var(env('FISERV_BLUEPAY_WEBHOOK_VERIFY_BP_STAMP', true), FILTER_VALIDATE_BOOL),
            ],
        ],
    ],

    'stored_value' => [
        'hold_ttl_seconds' => 120,
        'hold_heartbeat_seconds' => 60,
        'gift_card_code_prefix' => env('POS_GIFT_CARD_CODE_PREFIX', 'GC'),
    ],

    'billing' => [
        'enabled' => true,
    ],

    'leases' => [
        'table_assignment_ttl_seconds' => 30,
        'table_assignment_heartbeat_seconds' => 10,
        'order_edit_ttl_seconds' => 45,
        'order_edit_heartbeat_seconds' => 15,
    ],

    'exceptions' => [
        'register_close_variance_minor' => 2000,
        'register_close_variance_ratio_basis_points' => 100,
    ],

    'reporting' => [
        'connection' => env('POS_REPORTING_DB_CONNECTION', 'pgsql_reporting'),
        'fallback_to_primary' => filter_var(env('POS_REPORTING_FALLBACK_TO_PRIMARY', true), FILTER_VALIDATE_BOOL),
        'summary_staleness_seconds' => (int) env('POS_REPORTING_SUMMARY_STALENESS_SECONDS', 300),
    ],

    'retention' => [
        'sync_events_days' => (int) env('POS_RETENTION_SYNC_EVENTS_DAYS', 180),
        'device_status_events_days' => (int) env('POS_RETENTION_DEVICE_STATUS_EVENTS_DAYS', 365),
        'archive_access_logs_days' => (int) env('POS_RETENTION_ARCHIVE_ACCESS_LOGS_DAYS', 2555),
        'audit_logs_days' => (int) env('POS_RETENTION_AUDIT_LOGS_DAYS', 2555),
        'payroll_snapshots_days' => (int) env('POS_RETENTION_PAYROLL_SNAPSHOTS_DAYS', 2555),
        'receipts_days' => (int) env('POS_RETENTION_RECEIPTS_DAYS', 2555),
    ],

    'privacy' => [
        'export_table_limit' => (int) env('POS_PRIVACY_EXPORT_TABLE_LIMIT', 5000),
    ],

    'incidents' => [
        'dlq_stale_minutes' => (int) env('POS_DLQ_STALE_MINUTES', 15),
    ],

    'feature_flags' => [
        'definitions' => [
            'phase2-split-tender' => [
                'label' => 'Split Tender',
                'self_service' => true,
            ],
            'phase3-workforce' => [
                'label' => 'Workforce Operations',
                'self_service' => true,
            ],
            'phase4-delivery' => [
                'label' => 'Delivery Operations',
                'self_service' => true,
            ],
            'phase4-retail' => [
                'label' => 'Retail Operations',
                'self_service' => true,
            ],
        ],
    ],
];
