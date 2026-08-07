import { NextRequest } from 'next/server';
import { proxyToBackend } from '@/lib/server-proxy';

export const dynamic = 'force-dynamic';

type RouteContext = { params: Promise<{ path: string[] }> };

async function handler(request: NextRequest, context: RouteContext) {
  const { path } = await context.params;
  return proxyToBackend(request, path, '/actuator');
}

export const GET = handler;
