import React, { useEffect, useState } from 'react';
import { getCurrentUser, logout } from '../services/auth';

export default function Home() {
  const [user, setUser] = useState(() => getCurrentUser());

  useEffect(() => {
    const handler = () => setUser(getCurrentUser());
    window.addEventListener('auth-changed', handler);
    return () => window.removeEventListener('auth-changed', handler);
  }, []);

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <div style={{ fontFamily: 'sans-serif', padding: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ marginBottom: 8 }}>Welcome {user?.username || user?.email}</h1>
          <p style={{ margin: 0 }}>Role: {user?.role}</p>
        </div>
        <button onClick={handleLogout}>Logout</button>
      </div>

      <section style={{ marginTop: 24, border: '1px solid #ddd', borderRadius: 8, padding: 16 }}>
        <h2>Dashboard</h2>
        <p>Welcome to your home page. Navigate to Products to manage your inventory.</p>
      </section>
    </div>
  );
}
