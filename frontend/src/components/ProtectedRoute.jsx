import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { isAuthenticated, getCurrentUser } from '../services/auth';

export default function ProtectedRoute({ children, roles }) {
  const authed = isAuthenticated();
  if (!authed) return <Navigate to="/login" replace />;
  if (roles && roles.length > 0) {
    const user = getCurrentUser();
    const role = user?.role;
    const hasRole = role ? roles.includes(role) : false;
    if (!hasRole) return <Navigate to="/" replace />;
  }
  return children ? children : <Outlet />;
}

