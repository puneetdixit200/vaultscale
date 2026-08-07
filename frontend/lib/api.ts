import { clearToken, getToken } from './auth';

const API_BASE = '/api/v1';

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

function messageFromPayload(payload: unknown, fallback: string) {
  if (typeof payload === 'string' && payload.trim()) return payload;
  if (payload && typeof payload === 'object') {
    const values = Object.values(payload as Record<string, unknown>)
      .filter((value): value is string => typeof value === 'string' && value.length > 0);
    if (values.length) return values.join(' ');
  }
  return fallback;
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers = new Headers(init.headers);
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
    cache: 'no-store',
  });

  if (response.status === 401) {
    clearToken();
    if (!path.startsWith('/auth/')) window.location.assign('/login');
  }

  if (!response.ok) {
    let payload: unknown = null;
    try { payload = await response.json(); } catch { /* use status fallback */ }
    throw new ApiError(response.status, messageFromPayload(payload, `Request failed (${response.status})`));
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}
