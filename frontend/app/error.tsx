'use client';

export default function ErrorPage({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <main className="page-center">
      <section className="empty-state">
        <p className="eyebrow">Unexpected error</p>
        <h1>Something went wrong.</h1>
        <p>Try the action again. If it persists, check that the API is healthy.</p>
        <button type="button" onClick={reset}>Try again</button>
      </section>
    </main>
  );
}
