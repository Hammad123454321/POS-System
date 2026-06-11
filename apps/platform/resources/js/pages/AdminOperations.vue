<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { Head } from '@inertiajs/vue3';

const props = defineProps<{
    section?: string;
    auth?: {
        isSuperAdmin?: boolean;
        permissions?: string[];
    };
}>();

type FieldType = 'text' | 'number' | 'textarea';

type ActionField = {
    key: string;
    label: string;
    type?: FieldType;
    placeholder?: string;
    required?: boolean;
};

type WorkspaceAction = {
    title: string;
    method: 'GET' | 'POST' | 'PUT';
    path: string;
    intent: string;
    fields: ActionField[];
    destructive?: boolean;
};

type Workspace = {
    key: string;
    label: string;
    requiredPermission: string;
    eyebrow: string;
    title: string;
    description: string;
    highlights: string[];
    actions: WorkspaceAction[];
};

type OperationState = {
    busy: boolean;
    message: string;
    error: string;
};

const workspaces: Workspace[] = [
    {
        key: 'onboarding',
        label: 'Onboarding',
        requiredPermission: 'stores.manage',
        eyebrow: 'Stores and devices',
        title: 'Bring stores and registers online',
        description:
            'Create stores under your merchant, review device profiles, list registers, and mint single-use enrollment codes for new POS devices.',
        highlights: [
            'New stores inherit your merchant roles automatically.',
            'Enrollment codes are single-use and expire in 15 minutes.',
            'Deactivating a device blocks it from future sync.',
        ],
        actions: [
            {
                title: 'Create Store',
                method: 'POST',
                path: '/api/admin/v1/merchants/{merchant_id}/stores',
                intent: 'Add a new store location to your merchant.',
                fields: [
                    { key: 'merchant_id', label: 'Merchant ID', required: true },
                    { key: 'name', label: 'Store Name', required: true },
                    { key: 'code', label: 'Store Code' },
                    { key: 'mode', label: 'Mode (restaurant/retail/salon)', required: true },
                    { key: 'timezone', label: 'Timezone (e.g. America/New_York)', required: true },
                    { key: 'business_day_cutoff', label: 'Business Day Cutoff (HH:MM)', required: true },
                ],
            },
            {
                title: 'List Device Profiles',
                method: 'GET',
                path: '/api/admin/v1/device-profiles',
                intent: 'See available device profiles for enrollment.',
                fields: [],
            },
            {
                title: 'List Store Devices',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/devices',
                intent: 'List registers enrolled in a store.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'status', label: 'Status filter (optional)' },
                ],
            },
            {
                title: 'Mint Enrollment Code',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/device-enrollment-codes',
                intent: 'Generate a single-use code to enroll a new device.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'device_profile_id', label: 'Device Profile ID', required: true },
                ],
            },
            {
                title: 'Deactivate Device',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/devices/{device_id}/deactivate',
                intent: 'Disable a register so it can no longer sync.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'device_id', label: 'Device ID', required: true },
                ],
            },
        ],
    },
    {
        key: 'orders',
        label: 'Orders',
        requiredPermission: 'orders.view',
        eyebrow: 'Sales ledger',
        title: 'Browse orders and their details',
        description:
            'List orders for a store with filters by status and business date, then open a single order to see its lines, payments, refunds, voids, and receipt.',
        highlights: [
            'Reads from the reporting connection so it never loads the write primary.',
            'Filter by status, device, business-date range, or order number.',
            'Order detail includes payments with refunds and voids.',
        ],
        actions: [
            {
                title: 'List Orders',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/orders',
                intent: 'Browse a paginated list of orders for a store.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'status', label: 'Status (open/paid/voided)' },
                    { key: 'business_date_from', label: 'Business Date From (YYYY-MM-DD)' },
                    { key: 'business_date_to', label: 'Business Date To (YYYY-MM-DD)' },
                    { key: 'q', label: 'Order Number Search' },
                    { key: 'per_page', label: 'Per Page (max 100)', type: 'number' },
                ],
            },
            {
                title: 'Order Detail',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/orders/{order_id}',
                intent: 'Open a single order with lines, payments, refunds, and voids.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'order_id', label: 'Order ID', required: true },
                ],
            },
        ],
    },
    {
        key: 'catalog',
        label: 'Catalog',
        requiredPermission: 'catalog.manage',
        eyebrow: 'Menu and pricing',
        title: 'Keep the sellable catalog tidy',
        description:
            'Create variants, modifier groups, combo packages, tax rules, and discount rules from the same admin route family the POS already uses.',
        highlights: [
            'Variant and modifier changes are store-scoped.',
            'Deactivate actions preserve history instead of deleting records.',
            'Discount rules can be reused by POS and retail promotions.',
        ],
        actions: [
            {
                title: 'List Catalog Items',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/catalog/items',
                intent: 'Browse catalog items for a store.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'category_id', label: 'Category ID filter' },
                    { key: 'q', label: 'Name/SKU search' },
                ],
            },
            {
                title: 'Create Catalog Item',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/catalog/items',
                intent: 'Add a sellable product or service.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'name', label: 'Name', required: true },
                    { key: 'type', label: 'Type (product/service)', required: true },
                    { key: 'base_price_minor', label: 'Base Price Minor', type: 'number', required: true },
                    { key: 'currency', label: 'Currency (e.g. USD)', required: true },
                    { key: 'sku', label: 'SKU' },
                    { key: 'category_id', label: 'Category ID' },
                    { key: 'tax_rule_id', label: 'Tax Rule ID' },
                ],
            },
            {
                title: 'Create Category',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/catalog/categories',
                intent: 'Add a category to group catalog items.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'name', label: 'Category Name', required: true },
                    { key: 'sort_order', label: 'Sort Order', type: 'number' },
                ],
            },
            {
                title: 'Create Variant',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/catalog/variants',
                intent: 'Add a sellable SKU variant to a catalog item.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'catalog_item_id',
                        label: 'Catalog Item ID',
                        required: true,
                    },
                    { key: 'name', label: 'Variant Name', required: true },
                    {
                        key: 'price_minor',
                        label: 'Price Minor Units',
                        type: 'number',
                        required: true,
                    },
                    { key: 'sku', label: 'SKU' },
                ],
            },
            {
                title: 'Create Discount Rule',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/discount-rules',
                intent: 'Create an offer that can be used by POS or retail.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'name', label: 'Rule Name', required: true },
                    { key: 'code', label: 'Code' },
                    {
                        key: 'value_minor',
                        label: 'Value Minor Units',
                        type: 'number',
                    },
                    {
                        key: 'starts_at',
                        label: 'Starts At',
                        placeholder: '2026-05-06T09:00:00Z',
                    },
                ],
            },
        ],
    },
    {
        key: 'customers',
        label: 'Customers',
        requiredPermission: 'users.manage',
        eyebrow: 'Profiles and care',
        title: 'Look after customer records without touching sales history',
        description:
            'Create customer profiles, run privacy actions, and keep loyalty or membership work separate from financial records.',
        highlights: [
            'Customer creation stays merchant-owned through the store route.',
            'Privacy tools are audit-friendly and require a reason.',
            'Financial history remains intact when PII is removed.',
        ],
        actions: [
            {
                title: 'Create Customer',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/customers',
                intent: 'Add a customer profile for lookup and loyalty use.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'name', label: 'Name', required: true },
                    { key: 'phone', label: 'Phone' },
                    { key: 'email', label: 'Email' },
                ],
            },
        ],
    },
    {
        key: 'stored-value',
        label: 'Stored Value',
        requiredPermission: 'catalog.manage',
        eyebrow: 'Memberships',
        title: 'Manage plans that carry value over time',
        description:
            'Create membership plans and keep stored-value administration separate from day-to-day checkout.',
        highlights: [
            'Plans are created at store scope.',
            'POS reads active plans through the cloud config refresh.',
            'Membership records remain auditable.',
        ],
        actions: [
            {
                title: 'Create Membership Plan',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/membership-plans',
                intent: 'Add a membership plan the POS can sell or apply.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'name', label: 'Plan Name', required: true },
                    {
                        key: 'price_minor',
                        label: 'Price Minor Units',
                        type: 'number',
                    },
                    {
                        key: 'benefit_value_minor',
                        label: 'Benefit Minor Units',
                        type: 'number',
                    },
                ],
            },
        ],
    },
    {
        key: 'restaurant',
        label: 'Restaurant',
        requiredPermission: 'stores.manage',
        eyebrow: 'Dining room and print',
        title: 'Set up tables and route tickets cleanly',
        description:
            'Manage dining tables, print routes, and printer configs used by the POS and kitchen workflow.',
        highlights: [
            'Table setup supports claim and release behavior on POS.',
            'Print routes keep kitchen and receipt traffic separated.',
            'Printer config changes are store-scoped.',
        ],
        actions: [
            {
                title: 'Create Dining Table',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/dining-tables',
                intent: 'Add a table the POS can claim during service.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'name', label: 'Table Name', required: true },
                    {
                        key: 'capacity',
                        label: 'Capacity',
                        type: 'number',
                    },
                ],
            },
            {
                title: 'Create Print Route',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/print-routes',
                intent: 'Create a named route for kitchen or receipt printing.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'route_key', label: 'Route Key', required: true },
                    { key: 'name', label: 'Route Name', required: true },
                ],
            },
        ],
    },
    {
        key: 'workforce',
        label: 'Workforce',
        requiredPermission: 'users.manage',
        eyebrow: 'Staff and payroll',
        title: 'Keep salon and labor operations current',
        description:
            'Create staff profiles, service items, wage rules, and commission rules, then review labor analytics without leaving admin.',
        highlights: [
            'Staff profiles power appointments and shift controls in POS.',
            'Attendance approvals and payroll snapshots stay explicit.',
            'Labor reports are routed through the reporting connection.',
        ],
        actions: [
            {
                title: 'Create Staff Profile',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/workforce/staff-profiles',
                intent: 'Add a staff member for booking and shift tracking.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'display_name',
                        label: 'Display Name',
                        required: true,
                    },
                    { key: 'role_title', label: 'Role Title' },
                ],
            },
            {
                title: 'Open Labor Report',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/workforce/labor-analytics',
                intent: 'Review current labor and sales metrics.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'start_date', label: 'Start Date' },
                    { key: 'end_date', label: 'End Date' },
                ],
            },
        ],
    },
    {
        key: 'delivery',
        label: 'Delivery',
        requiredPermission: 'delivery.manage',
        eyebrow: 'Channels and availability',
        title: 'Control what delivery channels can sell',
        description:
            'Connect delivery channels, publish menus, and pause store availability without editing POS devices by hand.',
        highlights: [
            'Channel configs are managed per store.',
            'Store availability can be paused during rushes or outages.',
            'Menu publishing stays an explicit admin action.',
        ],
        actions: [
            {
                title: 'Create Channel Config',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/delivery/channel-configs',
                intent: 'Add a delivery integration configuration.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'channel_key',
                        label: 'Channel Key',
                        required: true,
                    },
                    {
                        key: 'external_store_id',
                        label: 'External Store ID',
                    },
                ],
            },
            {
                title: 'Set Store Availability',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/delivery/channel-configs/{delivery_channel_config_id}/store-availability',
                intent: 'Pause or resume a delivery channel for a store.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'delivery_channel_config_id',
                        label: 'Channel Config ID',
                        required: true,
                    },
                    {
                        key: 'is_available',
                        label: 'Available',
                        placeholder: 'true or false',
                        required: true,
                    },
                    { key: 'reason', label: 'Reason' },
                ],
            },
        ],
    },
    {
        key: 'retail',
        label: 'Retail',
        requiredPermission: 'catalog.manage',
        eyebrow: 'Inventory and promos',
        title: 'Move stock with a paper trail',
        description:
            'Record barcode data, create retail promotions, and keep inventory movements tied to a document and reason.',
        highlights: [
            'Receiving, transfer, adjustment, and return actions are available in POS.',
            'Promotions reuse the discount rule backbone.',
            'Stock movement summaries are read through reporting.',
        ],
        actions: [
            {
                title: 'Create Barcode Record',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/retail/barcode-records',
                intent: 'Attach a barcode to an item or SKU.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'barcode', label: 'Barcode', required: true },
                    { key: 'sku', label: 'SKU', required: true },
                ],
            },
            {
                title: 'Create Retail Promotion',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/retail/promotions',
                intent: 'Create a retail promotion backed by discount rules.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'name', label: 'Promotion Name', required: true },
                    { key: 'code', label: 'Code' },
                    {
                        key: 'value_minor',
                        label: 'Value Minor Units',
                        type: 'number',
                    },
                ],
            },
        ],
    },
    {
        key: 'reports',
        label: 'Reports',
        requiredPermission: 'reports.view',
        eyebrow: 'Read-only insight',
        title: 'Check the business without leaning on the write database',
        description:
            'Open operational reports for business-day health, delivery, and retail stock movement summaries.',
        highlights: [
            'Reporting queries use the reporting connection.',
            'Reports are intentionally read-only.',
            'Date filters can be passed directly from this workspace.',
        ],
        actions: [
            {
                title: 'Business Day Report',
                method: 'GET',
                path: '/api/pos/v1/reports/business-day-summary',
                intent: 'Open the current POS business-day summary endpoint.',
                fields: [
                    { key: 'business_date', label: 'Business Date' },
                    { key: 'store_id', label: 'Store ID' },
                ],
            },
            {
                title: 'Delivery Health',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/reports/delivery-health',
                intent: 'Review delivery operational health.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    { key: 'start_date', label: 'Start Date' },
                    { key: 'end_date', label: 'End Date' },
                ],
            },
        ],
    },
    {
        key: 'exceptions',
        label: 'Exceptions',
        requiredPermission: 'orders.view',
        eyebrow: 'Manual review',
        title: 'Resolve the work that needs a human',
        description:
            'Use exception routes for stuck sync, payment, delivery, and DLQ incidents that should not be handled silently.',
        highlights: [
            'DLQ monitoring creates critical exception cases automatically.',
            'Resolution is explicit and auditable.',
            'The dashboard shows open exception counts.',
        ],
        actions: [
            {
                title: 'Resolve Exception',
                method: 'POST',
                path: '/api/admin/v1/exception-cases/{exception_case_id}/resolve',
                intent: 'Mark an exception case as resolved with notes.',
                fields: [
                    {
                        key: 'exception_case_id',
                        label: 'Exception Case ID',
                        required: true,
                    },
                    {
                        key: 'resolution_notes',
                        label: 'Resolution Notes',
                        type: 'textarea',
                        required: true,
                    },
                ],
            },
        ],
    },
    {
        key: 'feature-flags',
        label: 'Feature Flags',
        requiredPermission: 'stores.manage',
        eyebrow: 'Controlled rollout',
        title: 'Change rollout behavior deliberately',
        description:
            'Update a store-scoped feature flag when an operational rollout needs a careful switch.',
        highlights: [
            'Flags are written through explicit admin routes.',
            'Useful for canaries, pilots, and emergency rollback.',
            'Changes should match the rollout notes in the runbook.',
        ],
        actions: [
            {
                title: 'Upsert Feature Flag',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/feature-flags/{flag_key}',
                intent: 'Create or update a store feature flag.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'flag_key',
                        label: 'Flag Key',
                        placeholder:
                            'phase2-split-tender, phase3-workforce, phase4-delivery, or phase4-retail',
                        required: true,
                    },
                    {
                        key: 'scope',
                        label: 'Scope',
                        placeholder: 'store or merchant',
                        required: true,
                    },
                    {
                        key: 'enabled',
                        label: 'Enabled',
                        placeholder: 'true or false',
                        required: true,
                    },
                    { key: 'notes', label: 'Notes' },
                ],
            },
        ],
    },
    {
        key: 'privacy',
        label: 'Privacy',
        requiredPermission: 'users.manage',
        eyebrow: 'Customer trust',
        title: 'Handle privacy requests carefully',
        description:
            'Create customer or merchant exports and tombstone direct PII while keeping invoices, payments, and audit records usable.',
        highlights: [
            'Every action should include a plain reason.',
            'Exports are written to private local storage.',
            'Tombstones remove direct PII without deleting financial records.',
        ],
        actions: [
            {
                title: 'Create Merchant Export',
                method: 'POST',
                path: '/api/admin/v1/merchants/{merchant_id}/privacy-export',
                intent: 'Package tenant-scoped operational records for a merchant privacy or portability request.',
                fields: [
                    {
                        key: 'merchant_id',
                        label: 'Merchant ID',
                        required: true,
                    },
                    { key: 'reason', label: 'Reason', required: true },
                ],
            },
            {
                title: 'Create Customer Export',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/customers/{customer_id}/privacy-export',
                intent: 'Package the customer data needed for a privacy request.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'customer_id',
                        label: 'Customer ID',
                        required: true,
                    },
                    { key: 'reason', label: 'Reason', required: true },
                ],
            },
            {
                title: 'Tombstone Customer PII',
                method: 'POST',
                path: '/api/admin/v1/stores/{store_id}/customers/{customer_id}/tombstone',
                intent: 'Remove direct PII while preserving operational history.',
                destructive: true,
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'customer_id',
                        label: 'Customer ID',
                        required: true,
                    },
                    { key: 'reason', label: 'Reason', required: true },
                ],
            },
        ],
    },
    {
        key: 'archive',
        label: 'Archive',
        requiredPermission: 'reports.view',
        eyebrow: 'Audited reads',
        title: 'Open archived records with a reason',
        description:
            'Archived receipt, audit, and payroll reads are logged immediately so support and compliance teams can explain who looked at what.',
        highlights: [
            'Archive reads write to archive_access_logs.',
            'Use a reason that would make sense in an audit review.',
            'This page does not bypass normal auth or tenant checks.',
        ],
        actions: [
            {
                title: 'Read Archived Receipt',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/archive/receipts/{receipt_id}',
                intent: 'Open a receipt archive record and log the access.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'receipt_id',
                        label: 'Receipt ID',
                        required: true,
                    },
                    { key: 'reason', label: 'Reason', required: true },
                ],
            },
            {
                title: 'Read Archived Audit Log',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/archive/audit-logs/{audit_log_id}',
                intent: 'Open an audit log record and log the access.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'audit_log_id',
                        label: 'Audit Log ID',
                        required: true,
                    },
                    { key: 'reason', label: 'Reason', required: true },
                ],
            },
            {
                title: 'Read Archived Payroll Snapshot',
                method: 'GET',
                path: '/api/admin/v1/stores/{store_id}/archive/payroll-snapshots/{payroll_snapshot_id}',
                intent: 'Open a payroll snapshot and log the access.',
                fields: [
                    { key: 'store_id', label: 'Store ID', required: true },
                    {
                        key: 'payroll_snapshot_id',
                        label: 'Payroll Snapshot ID',
                        required: true,
                    },
                    { key: 'reason', label: 'Reason', required: true },
                ],
            },
        ],
    },
];

const visibleWorkspaces = computed(() => {
    if (props.auth?.isSuperAdmin) return workspaces;

    const permissions = new Set(props.auth?.permissions ?? []);

    return workspaces.filter((workspace) =>
        permissions.has(workspace.requiredPermission),
    );
});

const sections = computed(() =>
    visibleWorkspaces.value.map((workspace) => workspace.key),
);
const activeSection = computed(() =>
    sections.value.includes(props.section ?? '')
        ? props.section!
        : (visibleWorkspaces.value[0]?.key ?? 'catalog'),
);
const activeWorkspace = computed(
    () =>
        visibleWorkspaces.value.find(
            (workspace) => workspace.key === activeSection.value,
        ) ??
        visibleWorkspaces.value[0] ??
        workspaces[0],
);

const operationStates = reactive<Record<string, OperationState>>({});
const formValues = reactive<Record<string, Record<string, string | number>>>(
    {},
);
const lastResponse = ref('');

const stateFor = (action: WorkspaceAction): OperationState => {
    operationStates[action.title] ??= {
        busy: false,
        message: '',
        error: '',
    };

    return operationStates[action.title];
};

const valuesFor = (
    action: WorkspaceAction,
): Record<string, string | number> => {
    formValues[action.title] ??= {};

    return formValues[action.title];
};

const csrfToken = (): string =>
    document
        .querySelector<HTMLMetaElement>('meta[name="csrf-token"]')
        ?.getAttribute('content') ?? '';

const normalizeValue = (value: string): string | number | boolean => {
    const trimmed = value.trim();

    if (trimmed === 'true') return true;
    if (trimmed === 'false') return false;
    if (/^-?\d+$/.test(trimmed)) return Number(trimmed);

    return trimmed;
};

const buildRequest = (
    action: WorkspaceAction,
    values: Record<string, string | number>,
) => {
    const payload: Record<string, string | number | boolean> = {};
    let path = action.path;

    for (const field of action.fields) {
        const rawValue = values[field.key] ?? '';
        const rawText = String(rawValue);
        const trimmed = rawText.trim();

        if (field.required && !trimmed) {
            throw new Error(`${field.label} is required.`);
        }

        if (!trimmed) continue;

        if (path.includes(`{${field.key}}`)) {
            path = path.replace(`{${field.key}}`, encodeURIComponent(trimmed));
        } else {
            payload[field.key] = normalizeValue(trimmed);
        }
    }

    const unresolved = path.match(/{[^}]+}/g);

    if (unresolved) {
        throw new Error(
            `Missing ${unresolved
                .map((item) => item.replace(/[{}]/g, '').replaceAll('_', ' '))
                .join(', ')}.`,
        );
    }

    return { path, payload };
};

const submitAction = async (action: WorkspaceAction) => {
    const state = stateFor(action);
    const values = valuesFor(action);

    state.busy = true;
    state.message = '';
    state.error = '';
    lastResponse.value = '';

    try {
        const { path, payload } = buildRequest(action, values);
        const query = new URLSearchParams();
        const options: RequestInit = {
            method: action.method,
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken(),
            },
            credentials: 'same-origin',
        };

        if (action.method === 'GET') {
            for (const [key, value] of Object.entries(payload)) {
                query.set(key, String(value));
            }
        } else {
            options.body = JSON.stringify(payload);
        }

        const response = await fetch(
            query.size > 0 ? `${path}?${query.toString()}` : path,
            options,
        );
        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            const message =
                data?.message ??
                data?.error ??
                `Request failed with ${response.status}.`;
            throw new Error(message);
        }

        state.message =
            action.method === 'GET'
                ? 'Opened successfully. The read was recorded where audit logging applies.'
                : 'Saved. The change is now in the admin API.';
        lastResponse.value = JSON.stringify(data, null, 2);
    } catch (error) {
        state.error =
            error instanceof Error
                ? error.message
                : 'Something went wrong while sending the request.';
    } finally {
        state.busy = false;
    }
};
</script>

<template>
    <Head :title="`${activeWorkspace.label} Admin`" />

    <main class="flex h-full flex-1 flex-col gap-5 p-4">
            <section
                class="border-b border-sidebar-border/70 pb-5"
            >
                <p class="text-sm font-medium text-muted-foreground">
                    {{ activeWorkspace.eyebrow }}
                </p>
                <h1 class="mt-1 max-w-4xl text-3xl font-semibold tracking-normal">
                    {{ activeWorkspace.title }}
                </h1>
                <p class="mt-3 max-w-3xl text-sm leading-6 text-muted-foreground">
                    {{ activeWorkspace.description }}
                </p>
            </section>

            <nav class="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
                <a
                    v-for="workspace in visibleWorkspaces"
                    :key="workspace.key"
                    :href="`/admin/${workspace.key}`"
                    class="rounded-md border px-3 py-3 text-sm transition hover:bg-muted"
                    :class="
                        activeSection === workspace.key
                            ? 'border-primary bg-muted font-semibold'
                            : 'border-sidebar-border/70'
                    "
                >
                    <span class="block">{{ workspace.label }}</span>
                    <span class="mt-1 block text-xs font-normal text-muted-foreground">
                        {{ workspace.eyebrow }}
                    </span>
                </a>
            </nav>

            <section class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
                <div class="grid gap-4 lg:grid-cols-2">
                    <form
                        v-for="action in activeWorkspace.actions"
                        :key="action.title"
                        class="rounded-lg border border-sidebar-border/70 bg-card p-5"
                        @submit.prevent="submitAction(action)"
                    >
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <p class="text-xs font-semibold uppercase text-muted-foreground">
                                    {{ action.method }}
                                </p>
                                <h2 class="mt-1 text-lg font-semibold">
                                    {{ action.title }}
                                </h2>
                            </div>
                            <span
                                v-if="action.destructive"
                                class="rounded-md border border-destructive/30 px-2 py-1 text-xs font-medium text-destructive"
                            >
                                Careful action
                            </span>
                        </div>
                        <p class="mt-2 text-sm leading-6 text-muted-foreground">
                            {{ action.intent }}
                        </p>

                        <div class="mt-4 grid gap-3">
                            <label
                                v-for="field in action.fields"
                                :key="field.key"
                                class="grid gap-1 text-sm font-medium"
                            >
                                <span>
                                    {{ field.label }}
                                    <span
                                        v-if="field.required"
                                        class="text-destructive"
                                    >
                                        *
                                    </span>
                                </span>
                                <textarea
                                    v-if="field.type === 'textarea'"
                                    v-model="valuesFor(action)[field.key]"
                                    class="min-h-24 rounded-md border bg-background px-3 py-2 text-sm font-normal"
                                    :placeholder="field.placeholder"
                                />
                                <input
                                    v-else
                                    v-model="valuesFor(action)[field.key]"
                                    :type="field.type === 'number' ? 'number' : 'text'"
                                    class="rounded-md border bg-background px-3 py-2 text-sm font-normal"
                                    :placeholder="field.placeholder"
                                />
                            </label>
                        </div>

                        <div class="mt-4 flex flex-wrap items-center gap-3">
                            <button
                                class="rounded-md px-4 py-2 text-sm font-medium disabled:cursor-not-allowed disabled:opacity-60"
                                :class="
                                    action.destructive
                                        ? 'bg-destructive text-destructive-foreground'
                                        : 'bg-primary text-primary-foreground'
                                "
                                :disabled="stateFor(action).busy"
                            >
                                {{
                                    stateFor(action).busy
                                        ? 'Working...'
                                        : action.method === 'GET'
                                          ? 'Open'
                                          : 'Save'
                                }}
                            </button>
                            <span
                                v-if="stateFor(action).message"
                                class="text-sm text-emerald-700"
                            >
                                {{ stateFor(action).message }}
                            </span>
                            <span
                                v-if="stateFor(action).error"
                                class="text-sm text-destructive"
                            >
                                {{ stateFor(action).error }}
                            </span>
                        </div>
                    </form>
                </div>

                <aside class="grid content-start gap-4">
                    <section
                        class="rounded-lg border border-sidebar-border/70 bg-card p-5"
                    >
                        <h2 class="text-base font-semibold">Good to know</h2>
                        <ul class="mt-3 grid gap-3 text-sm leading-6 text-muted-foreground">
                            <li
                                v-for="highlight in activeWorkspace.highlights"
                                :key="highlight"
                            >
                                {{ highlight }}
                            </li>
                        </ul>
                    </section>

                    <section
                        v-if="lastResponse"
                        class="rounded-lg border border-sidebar-border/70 bg-card p-5"
                    >
                        <h2 class="text-base font-semibold">Last response</h2>
                        <pre
                            class="mt-3 max-h-96 overflow-auto rounded-md bg-muted p-3 text-xs leading-5"
                        >{{ lastResponse }}</pre>
                    </section>
                </aside>
            </section>
    </main>
</template>
