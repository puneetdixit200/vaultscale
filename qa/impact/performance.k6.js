import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const authenticatedApiDuration = new Trend('authenticated_api_duration_ms', true);
const authenticatedApiFailures = new Counter('authenticated_api_failures');

export const options = {
  vus: Number(__ENV.VUS || 5),
  duration: __ENV.DURATION || '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
    authenticated_api_duration_ms: ['p(95)<800'],
  },
};

export function setup() {
  const suffix = `${Date.now()}-${Math.floor(Math.random() * 1_000_000)}`;
  const response = http.post(`${baseUrl}/api/v1/auth/register`, JSON.stringify({
    fullName: 'Impact Test',
    email: `impact-${suffix}@example.test`,
    password: 'ImpactTest123!',
  }), { headers: { 'Content-Type': 'application/json' } });

  const registered = check(response, {
    'setup registration returns 201': (result) => result.status === 201,
    'setup registration returns JWT': (result) => Boolean(result.json('token')),
  });
  if (!registered) throw new Error(`Could not create benchmark user: HTTP ${response.status}`);
  return { token: response.json('token') };
}

export default function ({ token }) {
  const params = { headers: { Authorization: `Bearer ${token}` } };
  for (const path of ['/api/v1/auth/me', '/api/v1/orgs']) {
    const response = http.get(`${baseUrl}${path}`, params);
    authenticatedApiDuration.add(response.timings.duration);
    const passed = check(response, { [`${path} is successful`]: (result) => result.status >= 200 && result.status < 300 });
    if (!passed) authenticatedApiFailures.add(1);
  }
  sleep(1);
}
