// frontend/src/features/endpoints/endpointsApi.ts
import api from '../../lib/api';

export interface Endpoint {
  id: string;
  name: string;
  method: string;
  url: string;
}

export interface RunResult {
  statusCode: number | null;
  responseBody: string | null;
  responseTimeMs: number;
  errorMessage: string | null;
}

export async function getEndpoints(orgId: string, collectionId: string) {
  const res = await api.get<Endpoint[]>(`/orgs/${orgId}/collections/${collectionId}/endpoints`);
  return res.data;
}

export async function createEndpoint(orgId: string, collectionId: string, name: string, method: string, url: string) {
  const res = await api.post<Endpoint>(`/orgs/${orgId}/collections/${collectionId}/endpoints`, { name, method, url, headers: {} });
  return res.data;
}

// Calls our SSRF-protected execution engine from Phase 4
export async function runEndpoint(orgId: string, collectionId: string, endpointId: string) {
  const res = await api.post<RunResult>(
    `/orgs/${orgId}/collections/${collectionId}/endpoints/${endpointId}/run`
  );
  return res.data;
}
