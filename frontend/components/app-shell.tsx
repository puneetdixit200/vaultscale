'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { clearToken } from '@/lib/auth';

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  function signOut() {
    clearToken();
    router.replace('/login');
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link className="brand" href="/dashboard" aria-label="VaultScale dashboard">
          <span className="brand-mark" aria-hidden="true">V</span>
          <span>VaultScale</span>
        </Link>
        <nav aria-label="Primary navigation">
          <Link className={pathname === '/dashboard' ? 'nav-link active' : 'nav-link'} href="/dashboard">Workspaces</Link>
        </nav>
        <button className="text-button" type="button" onClick={signOut}>Sign out</button>
      </header>
      <main className="app-main">{children}</main>
    </div>
  );
}
