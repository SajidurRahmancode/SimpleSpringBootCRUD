import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Products from './pages/Products';
import ProductForm from './pages/ProductForm';
import AdminLogin from './pages/AdminLogin';
import AdminSignup from './pages/AdminSignup';
import AdminUsers from './pages/AdminUsers';

export default function App() {
  return (
    <BrowserRouter>
      <div style={{ fontFamily: 'sans-serif', padding: 24 }}>
        <nav style={{ marginBottom: 16 }}>
          <Link to="/" style={{ marginRight: 12 }}>Home</Link>
          <Link to="/products" style={{ marginRight: 12 }}>Products</Link>
          <Link to="/login" style={{ marginRight: 12 }}>Login</Link>
          <Link to="/signup">Signup</Link>
          <span style={{ margin: '0 12px' }}>|</span>
          <Link to="/admin/login" style={{ marginRight: 12 }}>Admin Login</Link>
          <Link to="/admin/signup">Admin Signup</Link>
        </nav>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/admin/login" element={<AdminLogin />} />
          <Route path="/admin/signup" element={<AdminSignup />} />
          <Route path="/" element={<ProtectedRoute><Home /></ProtectedRoute>} />
          <Route path="/products" element={<ProtectedRoute><Products /></ProtectedRoute>} />
          <Route path="/products/new" element={<ProtectedRoute><ProductForm /></ProtectedRoute>} />
          <Route path="/products/:id/edit" element={<ProtectedRoute><ProductForm /></ProtectedRoute>} />
          <Route path="/admin/users" element={<ProtectedRoute roles={["ROLE_ADMIN"]}><AdminUsers /></ProtectedRoute>} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}
