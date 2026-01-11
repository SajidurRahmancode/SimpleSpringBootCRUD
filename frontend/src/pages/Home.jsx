import React from 'react';
import { getCurrentUser, logout } from '../services/auth';

export default function Home() {
  const user = getCurrentUser();

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <div style={{ fontFamily: 'sans-serif', padding: 24 }}>
      <h1>Welcome {user?.fullName || user?.email}</h1>
      <p>This is a protected page.</p>
      <button onClick={handleLogout}>Logout</button>
    </div>
  );
}

