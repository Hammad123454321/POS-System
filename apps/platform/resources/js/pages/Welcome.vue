<script setup lang="ts">
import { Head, Link } from '@inertiajs/vue3';
import { dashboard, login } from '@/routes';

// Registration is invite-only by default, so Wayfinder may not export a
// `register` route. Fall back to the static path so this page always compiles
// (the link itself is only shown when canRegister is true).
const register = () => '/register';

withDefaults(
    defineProps<{
        canRegister: boolean;
    }>(),
    {
        canRegister: true,
    },
);
</script>

<template>
    <Head title="POS Platform" />

    <main class="min-h-screen bg-[#f4f0e8] text-[#1f2d25]">
        <header class="mx-auto flex max-w-6xl items-center justify-between px-5 py-5">
            <div>
                <p class="text-xs font-semibold uppercase text-[#6f7f73]">
                    Multi-store commerce
                </p>
                <h1 class="text-xl font-semibold">POS Platform</h1>
            </div>

            <nav class="flex items-center gap-3 text-sm">
                <Link
                    v-if="$page.props.auth.user"
                    :href="dashboard()"
                    class="rounded-md bg-[#1f3a2e] px-4 py-2 font-medium text-white"
                >
                    Dashboard
                </Link>
                <template v-else>
                    <Link
                        :href="login()"
                        class="rounded-md border border-[#c9bdab] px-4 py-2 font-medium"
                    >
                        Log in
                    </Link>
                    <Link
                        v-if="canRegister"
                        :href="register()"
                        class="rounded-md bg-[#1f3a2e] px-4 py-2 font-medium text-white"
                    >
                        Register
                    </Link>
                </template>
            </nav>
        </header>

        <section class="mx-auto grid max-w-6xl gap-8 px-5 py-12 lg:grid-cols-[1fr_420px] lg:items-end">
            <div class="max-w-3xl">
                <p class="text-sm font-semibold text-[#6f7f73]">
                    Restaurant, salon, and retail operations in one place
                </p>
                <h2 class="mt-3 text-4xl font-semibold leading-tight md:text-5xl">
                    Run stores, devices, payments, orders, staff, delivery, and inventory from a single platform.
                </h2>
                <p class="mt-5 max-w-2xl text-base leading-7 text-[#526156]">
                    Built for multi-tenant POS teams that need offline-ready checkout, shared customer value, table service, workforce tools, retail stock movement, and clear back-office control.
                </p>

                <div class="mt-7 flex flex-wrap gap-3">
                    <Link
                        :href="$page.props.auth.user ? dashboard() : login()"
                        class="rounded-md bg-[#1f3a2e] px-5 py-3 text-sm font-semibold text-white"
                    >
                        Open back office
                    </Link>
                    <a
                        href="/admin/catalog"
                        class="rounded-md border border-[#c9bdab] px-5 py-3 text-sm font-semibold"
                    >
                        View admin workspaces
                    </a>
                </div>
            </div>

            <div class="rounded-lg border border-[#d8ccba] bg-white p-5 shadow-sm">
                <h3 class="text-base font-semibold">What is ready here</h3>
                <dl class="mt-4 grid gap-4 text-sm">
                    <div>
                        <dt class="font-semibold">POS device flows</dt>
                        <dd class="mt-1 text-[#526156]">
                            Register sessions, tendering, sync, printing, workforce, delivery, and retail actions.
                        </dd>
                    </div>
                    <div>
                        <dt class="font-semibold">Back-office workflows</dt>
                        <dd class="mt-1 text-[#526156]">
                            Catalog, customers, restaurant setup, staff, reports, feature flags, privacy, and archive access.
                        </dd>
                    </div>
                    <div>
                        <dt class="font-semibold">Production readiness</dt>
                        <dd class="mt-1 text-[#526156]">
                            Versioned APIs, tenant isolation, reporting routing, DLQ monitoring, retention jobs, and DR runbooks.
                        </dd>
                    </div>
                </dl>
            </div>
        </section>
    </main>
</template>
