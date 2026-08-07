'use client';

import Link from 'next/link';
import { FormEvent, useEffect, useState } from 'react';
import { AppShell } from '@/components/app-shell';
import { useAuthGuard } from '@/components/use-auth-guard';
import { ApiError } from '@/lib/api';
import type { Organization } from '@/lib/types';
import { organizationApi } from '@/lib/vaultscale-api';

function slugify(value: string) {
  return value.toLowerCase().trim().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
}

export function Dashboard() {
  const authenticated = useAuthGuard();
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  async function loadOrganizations() {
    setLoading(true);
    setError('');
    try {
      setOrganizations(await organizationApi.list());
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to load organizations.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (authenticated) void loadOrganizations();
  }, [authenticated]);

  async function createOrganization(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      const created = await organizationApi.create(name.trim(), slug.trim());
      setOrganizations((current) => [...current, created]);
      setName('');
      setSlug('');
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to create the organization.');
    } finally {
      setSubmitting(false);
    }
  }

  if (!authenticated) return <main className="page-center"><span className="spinner" aria-label="Checking session" /></main>;

  return (
    <AppShell>
      <section className="page-heading">
        <div><p className="eyebrow">Organizations</p><h1>Your API workspaces</h1><p className="muted">Keep collections, endpoints, and access under one team context.</p></div>
        <span className="count-badge">{organizations.length} {organizations.length === 1 ? 'workspace' : 'workspaces'}</span>
      </section>
      <section className="two-column">
        <div className="content-card">
          <div className="section-heading"><div><h2>Available workspaces</h2><p>Choose an organization to open its collections.</p></div><button className="icon-button" type="button" onClick={() => void loadOrganizations()} disabled={loading} aria-label="Refresh workspaces">↻</button></div>
          {error && <p className="form-error" role="alert">{error}</p>}
          {loading ? <div className="loading-row"><span className="spinner" /> Loading workspaces…</div> : organizations.length ? <ul className="resource-list">{organizations.map((organization) => <li key={organization.id}><Link href={`/orgs/${organization.id}/collections`}><span className="resource-icon">⌘</span><span><strong>{organization.name}</strong><small>{organization.slug}</small></span><span className="role-badge">{organization.yourRole}</span><span aria-hidden="true">→</span></Link></li>)}</ul> : <div className="empty-state compact"><h3>No workspaces yet</h3><p>Create your first organization to start saving API collections.</p></div>}
        </div>
        <aside className="content-card form-card">
          <p className="eyebrow">New workspace</p><h2>Create an organization</h2><p className="muted">You will become its owner.</p>
          <form className="form-stack" onSubmit={createOrganization}>
            <label>Name<input value={name} onChange={(event) => { const value = event.target.value; setName(value); if (!slug || slug === slugify(name)) setSlug(slugify(value)); }} placeholder="Platform team" required /></label>
            <label>URL slug<input value={slug} onChange={(event) => setSlug(slugify(event.target.value))} placeholder="platform-team" pattern="[a-z0-9]+(?:-[a-z0-9]+)*" required /></label>
            <button type="submit" disabled={submitting}>{submitting ? 'Creating…' : 'Create workspace'}</button>
          </form>
        </aside>
      </section>
    </AppShell>
  );
}
