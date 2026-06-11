<script setup lang="ts">
import { Head } from '@inertiajs/vue3';
import { reactive, ref } from 'vue';

defineProps<{
    section?: string;
}>();

const csrfToken = (): string =>
    document
        .querySelector<HTMLMetaElement>('meta[name="csrf-token"]')
        ?.getAttribute('content') ?? '';

const merchantForm = reactive({
    name: '',
    currency: 'USD',
});
const merchantStatus = ref('');
const merchantError = ref('');

const createMerchant = async () => {
    merchantStatus.value = '';
    merchantError.value = '';

    const response = await fetch('/api/super-admin/v1/merchants', {
        method: 'POST',
        headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken(),
        },
        credentials: 'same-origin',
        body: JSON.stringify(merchantForm),
    });
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        merchantError.value = data.message ?? 'Unable to create merchant.';
        return;
    }

    merchantStatus.value = `Created merchant ${data.data?.name ?? merchantForm.name}.`;
    merchantForm.name = '';
};
</script>

<template>
    <Head title="Super Admin Operations" />

    <main class="min-h-screen bg-slate-950 px-6 py-10 text-slate-100">
        <section class="mx-auto max-w-5xl">
            <p class="text-sm font-semibold uppercase tracking-[0.3em] text-emerald-300">
                Platform operations
            </p>
            <h1 class="mt-4 text-4xl font-black tracking-tight">
                Super Admin Console
            </h1>
            <p class="mt-3 max-w-2xl text-slate-300">
                Create merchants, provision canonical roles, and manage global platform state from a locked-down operations surface.
            </p>

            <div class="mt-10 grid gap-6 md:grid-cols-3">
                <article class="rounded-3xl border border-white/10 bg-white/10 p-6 shadow-2xl shadow-black/20 md:col-span-2">
                    <h2 class="text-2xl font-bold">Create Merchant</h2>
                    <p class="mt-2 text-sm text-slate-300">
                        Merchant creation automatically provisions Merchant Owner, Store Admin, Store Manager, and Cashier roles.
                    </p>

                    <form class="mt-6 grid gap-4 sm:grid-cols-[1fr_120px_auto]" @submit.prevent="createMerchant">
                        <label class="grid gap-2 text-sm">
                            <span class="font-medium text-slate-200">Name</span>
                            <input
                                v-model="merchantForm.name"
                                required
                                class="rounded-xl border border-white/10 bg-slate-900 px-4 py-3 text-slate-100 outline-none ring-emerald-300 transition focus:ring-2"
                            />
                        </label>
                        <label class="grid gap-2 text-sm">
                            <span class="font-medium text-slate-200">Currency</span>
                            <input
                                v-model="merchantForm.currency"
                                required
                                maxlength="3"
                                class="rounded-xl border border-white/10 bg-slate-900 px-4 py-3 uppercase text-slate-100 outline-none ring-emerald-300 transition focus:ring-2"
                            />
                        </label>
                        <button
                            type="submit"
                            class="self-end rounded-xl bg-emerald-300 px-5 py-3 font-bold text-slate-950 transition hover:bg-emerald-200"
                        >
                            Create
                        </button>
                    </form>

                    <p v-if="merchantStatus" class="mt-4 rounded-xl bg-emerald-400/10 px-4 py-3 text-sm text-emerald-200">
                        {{ merchantStatus }}
                    </p>
                    <p v-if="merchantError" class="mt-4 rounded-xl bg-rose-400/10 px-4 py-3 text-sm text-rose-200">
                        {{ merchantError }}
                    </p>
                </article>

                <aside class="rounded-3xl border border-white/10 bg-white/5 p-6">
                    <h2 class="text-xl font-bold">Workspaces</h2>
                    <ul class="mt-4 space-y-3 text-sm text-slate-300">
                        <li>Merchants API: `/api/super-admin/v1/merchants`</li>
                        <li>Global flags API: `/api/super-admin/v1/feature-flags`</li>
                        <li>Invitations: managed through role workflow</li>
                    </ul>
                </aside>
            </div>
        </section>
    </main>
</template>
