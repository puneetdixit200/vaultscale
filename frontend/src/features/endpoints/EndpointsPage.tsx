// frontend/src/features/endpoints/EndpointsPage.tsx

import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getEndpoints, createEndpoint, runEndpoint } from './endpointsApi';
import type { Endpoint, RunResult } from './endpointsApi';

export default function EndpointsPage() {
  const { orgId, collectionId } = useParams<{ orgId: string; collectionId: string }>();
  const [endpoints, setEndpoints] = useState<Endpoint[]>([]);
  const [name, setName] = useState('');
  const [method, setMethod] = useState('GET');
  const [url, setUrl] = useState('');
  const [runningId, setRunningId] = useState<string | null>(null);
  const [results, setResults] = useState<Record<string, RunResult>>({});

  useEffect(() => {
    if (orgId && collectionId) load();
  }, [orgId, collectionId]);

  async function load() {
    const data = await getEndpoints(orgId!, collectionId!);
    setEndpoints(data);
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    await createEndpoint(orgId!, collectionId!, name, method, url);
    setName(''); setUrl('');
    load();
  }

  // Runs the saved endpoint and stores the response keyed by endpoint ID,
  // so each row in the list can show its OWN independent result.
  async function handleRun(endpointId: string) {
    setRunningId(endpointId);
    try {
      const result = await runEndpoint(orgId!, collectionId!, endpointId);
      setResults((prev) => ({ ...prev, [endpointId]: result }));
    } finally {
      setRunningId(null);
    }
  }

  return (
    <div style={{ maxWidth: 700, margin: '40px auto', fontFamily: 'sans-serif' }}>
      <h1>Endpoints</h1>

      {endpoints.map((ep) => (
        <div key={ep.id} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 10 }}>
          <strong>{ep.method}</strong> {ep.name} — <code>{ep.url}</code>
          <button
            onClick={() => handleRun(ep.id)}
            disabled={runningId === ep.id}
            style={{ marginLeft: 10, padding: '4px 10px' }}
          >
            {runningId === ep.id ? 'Running...' : 'Run'}
          </button>

          {results[ep.id] && (
            <pre style={{ background: '#f5f5f5', padding: 8, marginTop: 8, overflow: 'auto' }}>
              Status: {results[ep.id].statusCode ?? 'N/A'} | {results[ep.id].responseTimeMs}ms
              {'\n'}{results[ep.id].errorMessage ?? results[ep.id].responseBody}
            </pre>
          )}
        </div>
      ))}

      <h2>Add Endpoint</h2>
      <form onSubmit={handleCreate}>
        <select value={method} onChange={(e) => setMethod(e.target.value)} style={{ marginRight: 8, padding: 8 }}>
          <option>GET</option><option>POST</option><option>PUT</option><option>DELETE</option><option>PATCH</option>
        </select>
        <input placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} required
          style={{ marginRight: 8, padding: 8 }} />
        <input placeholder="https://api.example.com/..." value={url} onChange={(e) => setUrl(e.target.value)} required
          style={{ marginRight: 8, padding: 8, width: 250 }} />
        <button type="submit" style={{ padding: 8 }}>Add</button>
      </form>
    </div>
  );
}
