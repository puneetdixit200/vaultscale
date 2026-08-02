// frontend/src/features/auth/authApi.ts
// TypeScript types + functions specific to authentication.
// Types here MUST match the shape of our Java DTOs (RegisterRequest, AuthResponse, etc.)

import api from '../../lib/api';

export interface AuthResponse {
  token: string;
  email: string;
  fullName: string;
}

export async function register(email: string, password: string, fullName: string) {
  // Generic<AuthResponse> tells TypeScript what shape to expect back
  const res = await api.post<AuthResponse>('/auth/register', { email, password, fullName });
  return res.data;
}

export async function login(email: string, password: string) {
  const res = await api.post<AuthResponse>('/auth/login', { email, password });
  return res.data;
}
