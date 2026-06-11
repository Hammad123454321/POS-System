<script setup lang="ts">
import { Head, useForm, usePage } from '@inertiajs/vue3';
import { computed } from 'vue';
import InputError from '@/components/InputError.vue';
import PasswordInput from '@/components/PasswordInput.vue';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const props = defineProps<{
    token: string;
    valid: boolean;
    email: string | null;
}>();

defineOptions({
    layout: {
        title: 'Accept your invitation',
        description: 'Set your name and password to join the team.',
    },
});

const form = useForm({
    name: '',
    password: '',
    password_confirmation: '',
});

const tokenError = computed(() => {
    const errors = usePage().props.errors as Record<string, string> | undefined;
    return errors?.token;
});

const submit = () => {
    form.post(`/invitations/${props.token}/accept`);
};
</script>

<template>
    <Head title="Accept invitation" />

    <div v-if="!valid" class="text-center text-sm text-muted-foreground">
        This invitation link is invalid, has expired, or has already been used.
    </div>

    <form v-else class="flex flex-col gap-6" @submit.prevent="submit">
        <div class="grid gap-6">
            <div class="grid gap-2">
                <Label for="email">Email</Label>
                <Input id="email" type="email" :model-value="email ?? ''" disabled />
            </div>

            <div class="grid gap-2">
                <Label for="name">Name</Label>
                <Input id="name" v-model="form.name" type="text" required autofocus autocomplete="name" />
                <InputError :message="form.errors.name" />
            </div>

            <div class="grid gap-2">
                <Label for="password">Password</Label>
                <PasswordInput id="password" v-model="form.password" required autocomplete="new-password" />
                <InputError :message="form.errors.password" />
            </div>

            <div class="grid gap-2">
                <Label for="password_confirmation">Confirm password</Label>
                <PasswordInput
                    id="password_confirmation"
                    v-model="form.password_confirmation"
                    required
                    autocomplete="new-password"
                />
            </div>

            <InputError :message="tokenError" />

            <Button type="submit" class="w-full" :disabled="form.processing">
                Accept invitation
            </Button>
        </div>
    </form>
</template>
