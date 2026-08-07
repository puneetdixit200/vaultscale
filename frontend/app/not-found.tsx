import Link from 'next/link';

export default function NotFound() {
  return (
    <main className="page-center">
      <section className="empty-state">
        <p className="eyebrow">404</p>
        <h1>Page not found</h1>
        <p>The route you requested does not exist in VaultScale.</p>
        <Link className="button" href="/dashboard">Go to dashboard</Link>
      </section>
    </main>
  );
}
