import { NextRequest } from 'next/server';

const API_ORIGIN = process.env.API_ORIGIN ?? 'http://localhost:8080';
const BODY_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

export async function proxyToBackend(request: NextRequest, path: string[], prefix: '/api/v1' | '/actuator') {
  const requestUrl = new URL(request.url);
  const target = new URL(`${prefix}/${path.map(encodeURIComponent).join('/')}`, API_ORIGIN);
  target.search = requestUrl.search;

  const headers = new Headers(request.headers);
  // The browser is same-origin with Next.js. Do not forward its Origin/Host to
  // Spring, whose CORS policy correctly only knows the public frontend origin.
  headers.delete('origin');
  headers.delete('host');
  headers.delete('connection');
  headers.delete('content-length');

  const upstream = await fetch(target, {
    method: request.method,
    headers,
    body: BODY_METHODS.has(request.method) ? await request.arrayBuffer() : undefined,
    cache: 'no-store',
    redirect: 'manual',
  });

  const responseHeaders = new Headers();
  for (const name of ['content-type', 'content-disposition', 'cache-control']) {
    const value = upstream.headers.get(name);
    if (value) responseHeaders.set(name, value);
  }
  return new Response(upstream.body, { status: upstream.status, headers: responseHeaders });
}
