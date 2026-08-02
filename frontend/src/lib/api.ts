// frontend/src/lib/api.ts
// Central HTTP client — every API call in the app goes through this file.
// Why centralize? So we only write auth-header logic ONCE, not in every component.

import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1', // matches our Spring Boot backend
});

// INTERCEPTOR: runs automatically before every request is sent.
// It reads the JWT saved in localStorage (after login) and attaches it as
// "Authorization: Bearer <token>" — exactly what our JwtAuthenticationFilter expects.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('vaultscale_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// INTERCEPTOR (response): if the backend ever returns 401 (token expired/invalid),
// automatically log the user out and send them back to /login.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('vaultscale_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
