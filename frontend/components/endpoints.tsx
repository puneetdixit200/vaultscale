'use client';

import Link from 'next/link';
import { FormEvent, useEffect, useState } from 'react';
import { AppShell } from '@/components/app-shell';
import { useAuthGuard } from '@/components/use-auth-guard';
import { ApiError } from '@/lib/api';
import type { Endpoint, HttpMethod, RunResult } from '@/lib/types';
import { endpointApi } from '@/lib/vaultscale-api';

const METHODS: HttpMethod[] = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'];

function formatResult(result: RunResult) {
  return `${result.statusCode ?? 'No HTTP status'} · ${result.responseTimeMs} ms\n\n${result.errorMessage ?? result.responseBody ?? 'No response body'}`;
}

export function Endpoints({ orgId, collectionId }: { orgId: string; collectionId: string }) {
  const authenticated = useAuthGuard();
  const [endpoints, setEndpoints] = useState<Endpoint[]>([]);
  const [name, setName] = useState('');
  const [method, setMethod] = useState<HttpMethod>('GET');
  const [url, setUrl] = useState('');
  const [headersText, setHeadersText] = useState('{}');
  const [body, setBody] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [runningId, setRunningId] = useState<string | null>(null);
  const [results, setResults] = useState<Record<string, RunResult>>({});
  const [error, setError] = useState('');

  async function loadEndpoints() {
    setLoading(true); setError('');
    try { setEndpoints(await endpointApi.list(orgId, collectionId)); }
    catch (cause) { setError(cause instanceof ApiError ? cause.message : 'Unable to load endpoints.'); }
    finally { setLoading(false); }
  }

  useEffect(() => { if (authenticated) void loadEndpoints(); }, [authenticated, orgId, collectionId]);

  async function createEndpoint(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError('');
    let headers: Record<string, string>;
    try {
      const parsed: unknown = JSON.parse(headersText || '{}');
      if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object' || Object.values(parsed).some((value) => typeof value !== 'string')) throw new Error();
      headers = parsed as Record<string, string>;
    } catch {
      setError('Headers must be a JSON object with string values, for example {"Accept":"application/json"}.');
      return;
    }
    setSubmitting(true);
    try {
      const created = await endpointApi.create(orgId, collectionId, { name: name.trim(), method, url: url.trim(), headers, body: body.trim() || null });
      setEndpoints((current) => [...current, created]);
      setName(''); setMethod('GET'); setUrl(''); setHeadersText('{}'); setBody('');
    } catch (cause) { setError(cause instanceof ApiError ? cause.message : 'Unable to save endpoint.'); }
    finally { setSubmitting(false); }
  }

  async function runEndpoint(endpointId: string) {
    setRunningId(endpointId); setError('');
    try {
      const result = await endpointApi.run(orgId, collectionId, endpointId);
      setResults((current) => ({ ...current, [endpointId]: result }));
    } catch (cause) { setError(cause instanceof ApiError ? cause.message : 'Unable to run the endpoint.'); }
    finally { setRunningId(null); }
  }

  if (!authenticated) return <main className="page-center"><span className="spinner" /></main>;
  return <AppShell>
    <div className="breadcrumb"><Link href="/dashboard">Workspaces</Link><span>/</span><Link href={`/orgs/${orgId}/collections`}>Collections</Link><span>/</span><span>Endpoints</span></div>
    <section className="page-heading"><div><p className="eyebrow">Collection workspace</p><h1>Endpoints</h1><p className="muted">Save request definitions, then run them through VaultScale’s guarded executor.</p></div></section>
    {error && <p className="form-error full-width" role="alert">{error}</p>}
    <section className="endpoint-layout">
      <div className="content-card endpoint-list-card">
        <div className="section-heading"><div><h2>Saved requests</h2><p>{endpoints.length ? 'Run requests individually and inspect their latest result.' : 'Create the first endpoint for this collection.'}</p></div><button className="icon-button" type="button" onClick={() => void loadEndpoints()} disabled={loading} aria-label="Refresh endpoints">↻</button></div>
        {loading ? <div className="loading-row"><span className="spinner" /> Loading endpoints…</div> : endpoints.length ? <div className="endpoint-list">{endpoints.map((endpoint) => <article className="endpoint-card" key={endpoint.id}><div className="endpoint-summary"><span className={`method method-${endpoint.method.toLowerCase()}`}>{endpoint.method}</span><div><h3>{endpoint.name}</h3><code>{endpoint.url}</code></div><button type="button" onClick={() => void runEndpoint(endpoint.id)} disabled={runningId === endpoint.id}>{runningId === endpoint.id ? 'Running…' : 'Run request'}</button></div>{results[endpoint.id] && <pre className={results[endpoint.id].errorMessage ? 'result result-error' : 'result'}>{formatResult(results[endpoint.id])}</pre>}</article>)}</div> : <div className="empty-state compact"><h3>No endpoints yet</h3><p>Save a request definition to run it safely from this workspace.</p></div>}
      </div>
      <aside className="content-card form-card endpoint-form-card"><p className="eyebrow">New endpoint</p><h2>Save a request</h2><form className="form-stack" onSubmit={createEndpoint}><label>Name<input value={name} onChange={(event) => setName(event.target.value)} placeholder="Get profile" required /></label><label>Method<select value={method} onChange={(event) => setMethod(event.target.value as HttpMethod)}>{METHODS.map((item) => <option key={item}>{item}</option>)}</select></label><label>URL<input type="url" value={url} onChange={(event) => setUrl(event.target.value)} placeholder="https://api.example.com/profile" required /></label><label>Headers <span className="label-optional">JSON object</span><textarea value={headersText} onChange={(event) => setHeadersText(event.target.value)} rows={4} spellCheck={false} /></label><label>Body <span className="label-optional">optional</span><textarea value={body} onChange={(event) => setBody(event.target.value)} rows={5} spellCheck={false} placeholder="Request body for POST, PUT, or PATCH" /></label><button type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Save endpoint'}</button></form></aside>
    </section>
  </AppShell>;
}
