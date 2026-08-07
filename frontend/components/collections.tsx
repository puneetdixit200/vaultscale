'use client';

import Link from 'next/link';
import { FormEvent, useEffect, useState } from 'react';
import { AppShell } from '@/components/app-shell';
import { useAuthGuard } from '@/components/use-auth-guard';
import { ApiError } from '@/lib/api';
import type { Collection } from '@/lib/types';
import { collectionApi } from '@/lib/vaultscale-api';

export function Collections({ orgId }: { orgId: string }) {
  const authenticated = useAuthGuard();
  const [collections, setCollections] = useState<Collection[]>([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  async function loadCollections() {
    setLoading(true); setError('');
    try { setCollections(await collectionApi.list(orgId)); }
    catch (cause) { setError(cause instanceof ApiError ? cause.message : 'Unable to load collections.'); }
    finally { setLoading(false); }
  }

  useEffect(() => { if (authenticated) void loadCollections(); }, [authenticated, orgId]);

  async function createCollection(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSubmitting(true); setError('');
    try {
      const created = await collectionApi.create(orgId, name.trim(), description.trim());
      setCollections((current) => [...current, created]); setName(''); setDescription('');
    } catch (cause) { setError(cause instanceof ApiError ? cause.message : 'Unable to create collection.'); }
    finally { setSubmitting(false); }
  }

  if (!authenticated) return <main className="page-center"><span className="spinner" /></main>;
  return <AppShell>
    <div className="breadcrumb"><Link href="/dashboard">Workspaces</Link><span>/</span><span>Collections</span></div>
    <section className="page-heading"><div><p className="eyebrow">Organization workspace</p><h1>Collections</h1><p className="muted">Group related API requests into shareable, team-ready collections.</p></div></section>
    <section className="two-column">
      <div className="content-card">
        <div className="section-heading"><div><h2>Saved collections</h2><p>{collections.length ? 'Open a collection to manage its endpoints.' : 'No collections have been created yet.'}</p></div><button className="icon-button" type="button" onClick={() => void loadCollections()} disabled={loading} aria-label="Refresh collections">↻</button></div>
        {error && <p className="form-error" role="alert">{error}</p>}
        {loading ? <div className="loading-row"><span className="spinner" /> Loading collections…</div> : collections.length ? <ul className="resource-list">{collections.map((collection) => <li key={collection.id}><Link href={`/orgs/${orgId}/collections/${collection.id}/endpoints`}><span className="resource-icon">▣</span><span><strong>{collection.name}</strong><small>{collection.description || 'No description provided'}</small></span><span aria-hidden="true">→</span></Link></li>)}</ul> : <div className="empty-state compact"><h3>Start with a collection</h3><p>Collections make it easy to organize endpoint definitions by product or service.</p></div>}
      </div>
      <aside className="content-card form-card"><p className="eyebrow">New collection</p><h2>Create a collection</h2><form className="form-stack" onSubmit={createCollection}><label>Name<input value={name} onChange={(event) => setName(event.target.value)} placeholder="Identity APIs" required /></label><label>Description <span className="label-optional">optional</span><textarea value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Endpoints used by the identity service" rows={3} /></label><button type="submit" disabled={submitting}>{submitting ? 'Creating…' : 'Create collection'}</button></form></aside>
    </section>
  </AppShell>;
}
