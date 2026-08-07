import { api } from './api';
import type { AuthResponse, Collection, Endpoint, HttpMethod, Organization, RunResult } from './types';

export const authApi = {
  register: (fullName: string, email: string, password: string) =>
    api<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify({ fullName, email, password }) }),
  login: (email: string, password: string) =>
    api<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
};

export const organizationApi = {
  list: () => api<Organization[]>('/orgs'),
  create: (name: string, slug: string) => api<Organization>('/orgs', { method: 'POST', body: JSON.stringify({ name, slug }) }),
};

export const collectionApi = {
  list: (orgId: string) => api<Collection[]>(`/orgs/${orgId}/collections`),
  create: (orgId: string, name: string, description: string) =>
    api<Collection>(`/orgs/${orgId}/collections`, { method: 'POST', body: JSON.stringify({ name, description }) }),
};

export const endpointApi = {
  list: (orgId: string, collectionId: string) => api<Endpoint[]>(`/orgs/${orgId}/collections/${collectionId}/endpoints`),
  create: (orgId: string, collectionId: string, endpoint: { name: string; method: HttpMethod; url: string; headers: Record<string, string>; body: string | null }) =>
    api<Endpoint>(`/orgs/${orgId}/collections/${collectionId}/endpoints`, { method: 'POST', body: JSON.stringify(endpoint) }),
  run: (orgId: string, collectionId: string, endpointId: string) =>
    api<RunResult>(`/orgs/${orgId}/collections/${collectionId}/endpoints/${endpointId}/run`, { method: 'POST' }),
};
