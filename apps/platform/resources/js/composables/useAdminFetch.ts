import { ref } from 'vue';

type Json = Record<string, unknown>;

function csrfToken(): string {
    return document.querySelector<HTMLMetaElement>('meta[name="csrf-token"]')?.content ?? '';
}

/**
 * Thin wrapper over fetch() mirroring the pattern used in AdminOperations.vue:
 * same-origin credentials, X-CSRF-TOKEN header, JSON body/responses, and
 * normalized error messages. Shared so new admin workspaces don't copy-paste.
 */
export function useAdminFetch() {
    const loading = ref(false);
    const error = ref<string | null>(null);

    async function request<T = Json>(
        method: string,
        path: string,
        options: { query?: Record<string, string | number | undefined>; body?: Json } = {},
    ): Promise<T | null> {
        loading.value = true;
        error.value = null;

        let url = path;
        if (options.query) {
            const params = new URLSearchParams();
            for (const [key, value] of Object.entries(options.query)) {
                if (value !== undefined && value !== '') {
                    params.append(key, String(value));
                }
            }
            const qs = params.toString();
            if (qs) {
                url += (url.includes('?') ? '&' : '?') + qs;
            }
        }

        try {
            const response = await fetch(url, {
                method,
                credentials: 'same-origin',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken(),
                },
                body: options.body ? JSON.stringify(options.body) : undefined,
            });

            const data = (await response.json().catch(() => null)) as Json | null;

            if (!response.ok) {
                error.value =
                    (data?.message as string) ??
                    (data?.error as string) ??
                    `Request failed with status ${response.status}`;
                return null;
            }

            return data as T;
        } catch (e) {
            error.value = e instanceof Error ? e.message : 'Network error';
            return null;
        } finally {
            loading.value = false;
        }
    }

    return {
        loading,
        error,
        get: <T = Json>(path: string, query?: Record<string, string | number | undefined>) =>
            request<T>('GET', path, { query }),
        post: <T = Json>(path: string, body?: Json) => request<T>('POST', path, { body }),
        put: <T = Json>(path: string, body?: Json) => request<T>('PUT', path, { body }),
        del: <T = Json>(path: string) => request<T>('DELETE', path),
    };
}
