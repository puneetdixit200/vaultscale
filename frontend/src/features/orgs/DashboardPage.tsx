// frontend/src/features/orgs/DashboardPage.tsx

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyOrgs, createOrg, Org } from './orgsApi';

export default function DashboardPage() {
  const [orgs, setOrgs] = useState<Org[]>([]);
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [loading, setLoading] = useState(true);

  // useEffect runs once when this component first renders (empty [] dependency array)
  useEffect(() => {
    loadOrgs();
  }, []);

  async function loadOrgs() {
    setLoading(true);
    const data = await getMyOrgs();
    setOrgs(data);
    setLoading(false);
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    await createOrg(name, slug);
    setName('');
    setSlug('');
    loadOrgs(); // refresh the list after creating
  }

  return (
    <div style={{ maxWidth: 600, margin: '40px auto', fontFamily: 'sans-serif' }}>
      <h1>Your Organizations</h1>

      {loading ? <p>Loading...</p> : (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {orgs.map((org) => (
            <li key={org.id} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 8 }}>
              <Link to={`/orgs/${org.id}/collections`}>
                <strong>{org.name}</strong> ({org.slug})
              </Link>
              <span style={{ float: 'right', color: '#888' }}>{org.yourRole}</span>
            </li>
          ))}
        </ul>
      )}

      <h2>Create New Organization</h2>
      <form onSubmit={handleCreate}>
        <input placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} required
          style={{ marginRight: 8, padding: 8 }} />
        <input placeholder="slug-like-this" value={slug} onChange={(e) => setSlug(e.target.value)} required
          style={{ marginRight: 8, padding: 8 }} />
        <button type="submit" style={{ padding: 8 }}>Create</button>
      </form>
    </div>
  );
}
