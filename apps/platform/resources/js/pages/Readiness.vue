<script setup lang="ts">
import { Head } from '@inertiajs/vue3';

const sections = [
    {
        title: 'Tenant Isolation',
        body: 'Every tenant-scoped table carries merchant context, and PostgreSQL RLS is applied as a second line of defense in production.',
    },
    {
        title: 'Queues And DLQ',
        body: 'Payments, sync, delivery, reporting, and telemetry queues are separated by connection or Redis database. Stale failed jobs are raised as critical exception cases.',
    },
    {
        title: 'Reporting Replica',
        body: 'Business-day, delivery-health, and retail stock movement reports are routed through the reporting database connection.',
    },
    {
        title: 'Privacy And Archive',
        body: 'Customer exports and PII tombstones are available from admin. Archived receipt reads are logged with actor, store, reason, and timestamp.',
    },
    {
        title: 'Deployment And DR',
        body: 'Canary and failover scripts live in the repository, with runbooks covering rollout, rollback, Route 53 cutover, workers, and backups.',
    },
    {
        title: 'Payment Readiness',
        body: 'PAX/POSLink code paths exist, while production acceptance still depends on terminal boarding, live credentials, and processor certification evidence.',
    },
];
</script>

<template>
    <Head title="Readiness" />

    <main class="flex h-full flex-1 flex-col gap-5 p-4">
        <section class="border-b border-sidebar-border/70 pb-5">
            <p class="text-sm font-medium text-muted-foreground">
                Production readiness
            </p>
            <h1 class="mt-1 text-3xl font-semibold">Operational Runbook</h1>
            <p class="mt-3 max-w-3xl text-sm leading-6 text-muted-foreground">
                A quick field guide for the pieces that matter before a store
                trusts this platform for live checkout, delivery, workforce,
                and back-office operations.
            </p>
        </section>

        <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <article
                v-for="section in sections"
                :key="section.title"
                class="rounded-lg border border-sidebar-border/70 bg-card p-5"
            >
                <h2 class="font-semibold">{{ section.title }}</h2>
                <p class="mt-2 text-sm leading-6 text-muted-foreground">
                    {{ section.body }}
                </p>
            </article>
        </section>

        <section class="rounded-lg border border-sidebar-border/70 bg-card p-5">
            <h2 class="font-semibold">Still outside local verification</h2>
            <p class="mt-2 text-sm leading-6 text-muted-foreground">
                Local PostgreSQL-backed verification still needs PHP
                <code>pdo_pgsql</code>. Live card acceptance, immutable
                backups, production alert routing, and DNS failover also depend
                on environment-owned credentials and infrastructure.
            </p>
        </section>
    </main>
</template>
