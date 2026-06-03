<script setup lang="ts">
import { Head, Link } from '@inertiajs/vue3';
import {
    AlertTriangle,
    BarChart3,
    CalendarClock,
    ClipboardList,
    Flag,
    PackageSearch,
    RadioTower,
    ReceiptText,
    ShieldCheck,
    Truck,
    Users,
    WalletCards,
} from 'lucide-vue-next';
import { dashboard } from '@/routes';

defineOptions({
    layout: {
        breadcrumbs: [
            {
                title: 'Dashboard',
                href: dashboard(),
            },
        ],
    },
});

const metricCards = [
    {
        label: 'Exception Queue',
        value: 'Store Review',
        detail: 'Sync, payment, delivery, inventory, and register conflicts',
        icon: AlertTriangle,
    },
    {
        label: 'Sync Health',
        value: 'Device Lag',
        detail: 'Pending events, auth-hold events, and recovery runs',
        icon: RadioTower,
    },
    {
        label: 'Payments',
        value: 'In-Doubt Watch',
        detail: 'Inquiry, reversal, refund, and void recovery',
        icon: WalletCards,
    },
    {
        label: 'Reporting',
        value: 'Replica Only',
        detail: 'Business day, delivery health, and retail stock movement',
        icon: BarChart3,
    },
];

const operations = [
    {
        title: 'Catalog & Pricing',
        body: 'Variants, modifiers, combos, add-ons, discounts, taxes, and retail promotions.',
        icon: ClipboardList,
        href: '/admin/catalog',
    },
    {
        title: 'Customers & Stored Value',
        body: 'Customers, member accounts, membership plans, gift cards, and ledger-backed balances.',
        icon: Users,
        href: '/admin/customers',
    },
    {
        title: 'Restaurant Operations',
        body: 'Dining tables, table leases, printer configs, and print routes.',
        icon: ReceiptText,
        href: '/admin/restaurant',
    },
    {
        title: 'Workforce',
        body: 'Staff profiles, service rules, appointments, shifts, attendance, wages, commissions, and payroll snapshots.',
        icon: CalendarClock,
        href: '/admin/workforce',
    },
    {
        title: 'Delivery',
        body: 'Channel config, menu publishing, order ingest, status propagation, and availability controls.',
        icon: Truck,
        href: '/admin/delivery',
    },
    {
        title: 'Retail Inventory',
        body: 'SKU/barcode records, receiving, transfers, adjustments, returns, stock reports, and promotion controls.',
        icon: PackageSearch,
        href: '/admin/retail',
    },
    {
        title: 'Feature Flags',
        body: 'Merchant-scoped rollout flags with store-level overrides.',
        icon: Flag,
        href: '/admin/feature-flags',
    },
    {
        title: 'Security & Readiness',
        body: 'Tenant isolation, archived access, device quarantine, and operational runbooks.',
        icon: ShieldCheck,
        href: '/docs/readiness',
    },
];
</script>

<template>
    <Head title="Operations Dashboard" />

    <main class="flex h-full flex-1 flex-col gap-6 overflow-x-auto p-4">
        <section class="grid gap-4 md:grid-cols-4">
            <article
                v-for="card in metricCards"
                :key="card.label"
                class="rounded-lg border border-sidebar-border/70 bg-card p-4 shadow-sm dark:border-sidebar-border"
            >
                <component
                    :is="card.icon"
                    class="mb-4 size-5 text-muted-foreground"
                />
                <p class="text-sm text-muted-foreground">{{ card.label }}</p>
                <h2 class="mt-1 text-xl font-semibold text-foreground">
                    {{ card.value }}
                </h2>
                <p class="mt-2 text-sm leading-5 text-muted-foreground">
                    {{ card.detail }}
                </p>
            </article>
        </section>

        <section
            class="rounded-lg border border-sidebar-border/70 bg-card p-5 shadow-sm dark:border-sidebar-border"
        >
            <div class="mb-5 flex flex-wrap items-end justify-between gap-3">
                <div>
                    <p class="text-sm font-medium text-muted-foreground">
                        Back Office
                    </p>
                    <h1 class="text-2xl font-semibold text-foreground">
                        Operations Control
                    </h1>
                </div>
                <Link
                    href="/horizon"
                    class="rounded-md border border-sidebar-border px-3 py-2 text-sm font-medium text-foreground transition hover:bg-muted"
                >
                    Horizon
                </Link>
            </div>

            <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                <Link
                    v-for="operation in operations"
                    :key="operation.title"
                    :href="operation.href"
                    class="rounded-lg border border-sidebar-border/70 p-4 transition hover:border-primary/50 hover:bg-muted/40"
                >
                    <component
                        :is="operation.icon"
                        class="mb-4 size-5 text-muted-foreground"
                    />
                    <h2 class="font-semibold text-foreground">
                        {{ operation.title }}
                    </h2>
                    <p class="mt-2 text-sm leading-5 text-muted-foreground">
                        {{ operation.body }}
                    </p>
                </Link>
            </div>
        </section>
    </main>
</template>
