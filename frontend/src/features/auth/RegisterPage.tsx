// frontend/src/features/auth/RegisterPage.tsx
// Almost identical structure to LoginPage — this is a common React pattern.

import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from './authApi';

export default function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    try {
      const response = await register(email, password, fullName);
      localStorage.setItem('vaultscale_token', response.token);
      navigate('/dashboard');
    } catch (err: any) {
      // err.response.data is our GlobalExceptionHandler's JSON error map
      setError(Object.values(err.response?.data || {}).join(', ') || 'Registration failed');
    }
  }

  return (
    <div style={{ maxWidth: 400, margin: '80px auto', fontFamily: 'sans-serif' }}>
      <h1>Create VaultScale Account</h1>
      <form onSubmit={handleSubmit}>
        <input placeholder="Full Name" value={fullName}
          onChange={(e) => setFullName(e.target.value)} required
          style={{ display: 'block', width: '100%', marginBottom: 10, padding: 8 }} />
        <input type="email" placeholder="Email" value={email}
          onChange={(e) => setEmail(e.target.value)} required
          style={{ display: 'block', width: '100%', marginBottom: 10, padding: 8 }} />
        <input type="password" placeholder="Password (min 8 chars)" value={password}
          onChange={(e) => setPassword(e.target.value)} required
          style={{ display: 'block', width: '100%', marginBottom: 10, padding: 8 }} />
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit" style={{ width: '100%', padding: 10 }}>Register</button>
      </form>
      <p>Already have an account? <Link to="/login">Log in</Link></p>
    </div>
  );
}
