import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminSignup } from '../services/auth';

export default function AdminSignup() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [adminSecret, setAdminSecret] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await adminSignup({ username, email, password, adminSecret });
      navigate('/admin/users');
    } catch (e) {
      const msg = e?.response?.data?.error || e?.response?.data?.message || 'Admin signup failed';
      setError(msg);
    }
  };

  return (
    <div>
      <h2>Admin Signup</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit} style={{ maxWidth: 400 }}>
        <div style={{ marginBottom: 12 }}>
          <label>Username</label><br />
          <input value={username} onChange={e => setUsername(e.target.value)} required style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Email</label><br />
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Password</label><br />
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Admin Secret</label><br />
          <input value={adminSecret} onChange={e => setAdminSecret(e.target.value)} required style={{ width: '100%' }} />
        </div>
        <button type="submit">Sign up</button>
      </form>
    </div>
  );
}
