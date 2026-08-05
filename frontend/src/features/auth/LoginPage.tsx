
// frontend/src/features/auth/LoginPage.tsx

import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from './authApi';

export default function LoginPage() {
  // useState: React's way of storing values that, when changed, re-render the UI
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // Runs when the form is submitted (button clicked or Enter pressed)
  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault(); // stops the browser's default full-page-reload form behavior
    setError('');

    try {
      const response = await login(email, password);
      // Save the JWT — our api.ts interceptor will read this on every future request
      localStorage.setItem('vaultscale_token', response.token);
      navigate('/dashboard'); // redirect on success
    } catch (err: any) {
      setError(err.response?.data?.error || 'Login failed. Check your credentials.');
    }
  }

  return (
    <div style={{ maxWidth: 400, margin: '80px auto', fontFamily: 'sans-serif' }}>
      <h1>VaultScale Login</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)} // updates state on every keystroke
          required
          style={{ display: 'block', width: '100%', marginBottom: 10, padding: 8 }}
        />
        <input
          type="password"
          placeholder="Password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          style={{ display: 'block', width: '100%', marginBottom: 10, padding: 8 }}
        />
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit" style={{ width: '100%', padding: 10 }}>Log In</button>
      </form>
      <p>No account? <Link to="/register">Register here</Link></p>
    </div>
  );
}
