export type Role = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';

export interface AuthResponse {
  token: string;
  email: string;
  fullName: string;
}

export interface Organization {
  id: string;
  name: string;
  slug: string;
  yourRole: Role;
}

export interface Collection {
  id: string;
  name: string;
  description: string | null;
}

export interface Endpoint {
  id: string;
  name: string;
  method: HttpMethod;
  url: string;
  headers: Record<string, string> | null;
  body: string | null;
}

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface RunResult {
  statusCode: number | null;
  responseBody: string | null;
  responseTimeMs: number;
  errorMessage: string | null;
}
