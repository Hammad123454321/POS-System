<script setup lang="ts" generic="T extends Record<string, unknown>">
import { Button } from '@/components/ui/button';

export type Column<Row> = {
    key: string;
    label: string;
    align?: 'left' | 'right' | 'center';
    format?: (row: Row) => string;
};

const props = defineProps<{
    columns: Column<T>[];
    rows: T[];
    pagination?: { page: number; perPage: number; total: number };
    loading?: boolean;
}>();

const emit = defineEmits<{
    (e: 'update:page', page: number): void;
    (e: 'row-click', row: T): void;
}>();

const cellValue = (row: T, column: Column<T>): string => {
    if (column.format) {
        return column.format(row);
    }
    const value = row[column.key];
    return value === null || value === undefined ? '' : String(value);
};

const lastPage = () =>
    props.pagination ? Math.max(1, Math.ceil(props.pagination.total / props.pagination.perPage)) : 1;
</script>

<template>
    <div class="overflow-hidden rounded-lg border border-border">
        <table class="w-full text-sm">
            <thead class="bg-muted/50">
                <tr>
                    <th
                        v-for="column in columns"
                        :key="column.key"
                        class="px-4 py-2 font-medium text-muted-foreground"
                        :class="{
                            'text-right': column.align === 'right',
                            'text-center': column.align === 'center',
                            'text-left': !column.align || column.align === 'left',
                        }"
                    >
                        {{ column.label }}
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr v-if="loading">
                    <td :colspan="columns.length" class="px-4 py-6 text-center text-muted-foreground">
                        Loading…
                    </td>
                </tr>
                <tr v-else-if="rows.length === 0">
                    <td :colspan="columns.length" class="px-4 py-6 text-center text-muted-foreground">
                        No records found.
                    </td>
                </tr>
                <tr
                    v-for="(row, index) in rows"
                    v-else
                    :key="index"
                    class="cursor-pointer border-t border-border hover:bg-muted/30"
                    @click="emit('row-click', row)"
                >
                    <td
                        v-for="column in columns"
                        :key="column.key"
                        class="px-4 py-2"
                        :class="{
                            'text-right': column.align === 'right',
                            'text-center': column.align === 'center',
                        }"
                    >
                        {{ cellValue(row, column) }}
                    </td>
                </tr>
            </tbody>
        </table>

        <div
            v-if="pagination"
            class="flex items-center justify-between border-t border-border px-4 py-2 text-sm text-muted-foreground"
        >
            <span>
                Page {{ pagination.page }} of {{ lastPage() }} · {{ pagination.total }} total
            </span>
            <div class="flex gap-2">
                <Button
                    variant="outline"
                    size="sm"
                    :disabled="pagination.page <= 1"
                    @click="emit('update:page', pagination.page - 1)"
                >
                    Previous
                </Button>
                <Button
                    variant="outline"
                    size="sm"
                    :disabled="pagination.page >= lastPage()"
                    @click="emit('update:page', pagination.page + 1)"
                >
                    Next
                </Button>
            </div>
        </div>
    </div>
</template>
