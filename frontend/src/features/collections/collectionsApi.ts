// frontend/src/features/collections/collectionsApi.ts
import api from '../../lib/api';

export interface Collection {
  id: string;
  name: string;
  description: string;
}

export async function getCollections(orgId: string) {
  const res = await api.get<Collection[]>(`/orgs/${orgId}/collections`);
  return res.data;
}

export async function createCollection(orgId: string, name: string, description: string) {
  const res = await api.post<Collection>(`/orgs/${orgId}/collections`, { name, description });
  return res.data;
}
