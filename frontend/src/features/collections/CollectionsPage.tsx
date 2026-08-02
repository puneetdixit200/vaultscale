// frontend/src/features/collections/CollectionsPage.tsx

import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCollections, createCollection, Collection } from './collectionsApi';

export default function CollectionsPage() {
  // useParams reads dynamic segments from the URL, e.g. /orgs/:orgId/collections
  const { orgId } = useParams<{ orgId: string }>();
  const [collections, setCollections] = useState<Collection[]>([]);
  const [name, setName] = useState('');

  useEffect(() => {
    if (orgId) loadCollections();
  }, [orgId]); // re-runs if the orgId in the URL ever changes

  async function loadCollections() {
    const data = await getCollections(orgId!);
    setCollections(data);
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    await createCollection(orgId!, name, '');
    setName('');
    loadCollections();
  }

  return (
    <div style={{ maxWidth: 600, margin: '40px auto', fontFamily: 'sans-serif' }}>
      <h1>Collections</h1>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {collections.map((c) => (
          <li key={c.id} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 8 }}>
            <Link to={`/orgs/${orgId}/collections/${c.id}/endpoints`}>{c.name}</Link>
          </li>
        ))}
      </ul>
      <form onSubmit={handleCreate}>
        <input placeholder="Collection name" value={name} onChange={(e) => setName(e.target.value)} required
          style={{ marginRight: 8, padding: 8 }} />
        <button type="submit" style={{ padding: 8 }}>Create Collection</button>
      </form>
    </div>
  );
}
