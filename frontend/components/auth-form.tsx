'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { FormEvent, useState } from 'react';
import { storeToken } from '@/lib/auth';
import { ApiError } from '@/lib/api';
import { authApi } from '@/lib/vaultscale-api';

export function AuthForm({ mode }: { mode: 'login' | 'register' }) {
  const router = useRouter();
  const isRegister = mode === 'register';
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const result = isRegister
        ? await authApi.register(fullName.trim(), email.trim(), password)
        : await authApi.login(email.trim(), password);
      storeToken(result.token);
      router.replace('/dashboard');
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to reach VaultScale. Try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-panel">
        <Link className="brand" href="/dashboard"><span className="brand-mark">V</span><span>VaultScale</span></Link>
        <p className="eyebrow">Secure API workspace</p>
        <h1>{isRegister ? 'Create your workspace account' : 'Welcome back'}</h1>
        <p className="muted">{isRegister ? 'Start organizing and safely running your team’s API requests.' : 'Sign in to manage your organizations and endpoint collections.'}</p>
        <form className="form-stack" onSubmit={submit}>
          {isRegister && <label>Full name<input value={fullName} onChange={(event) => setFullName(event.target.value)} autoComplete="name" required /></label>}
          <label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" required /></label>
          <label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete={isRegister ? 'new-password' : 'current-password'} minLength={isRegister ? 8 : undefined} required /></label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button type="submit" disabled={submitting}>{submitting ? 'Please wait…' : isRegister ? 'Create account' : 'Sign in'}</button>
        </form>
        <p className="auth-switch">{isRegister ? 'Already have an account?' : 'New to VaultScale?'} <Link href={isRegister ? '/login' : '/register'}>{isRegister ? 'Sign in' : 'Create an account'}</Link></p>
      </section>
      <aside className="auth-aside" aria-label="VaultScale benefits">
        <p className="eyebrow">Built for API work</p>
        <h2>Save context. Run safely. Keep a clear trail.</h2>
        <ul>
          <li>Role-aware organization workspaces</li>
          <li>Reusable collections and endpoint definitions</li>
          <li>SSRF-aware request execution with history</li>
        </ul>
      </aside>
    </main>
  );
}
