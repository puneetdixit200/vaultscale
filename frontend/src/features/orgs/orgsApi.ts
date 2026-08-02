// frontend/src/features/orgs/orgsApi.ts
import api from '../../lib/api';

export interface Org {
  id: string;
  name: string;
  slug: string;
  yourRole: string;
}

export async function getMyOrgs() {
  const res = await api.get<Org[]>('/orgs');
  return res.data;
}

export async function createOrg(name: string, slug: string) {
  const res = await api.post<Org>('/orgs', { name, slug });
  return res.data;
}
