import api from './api';

export async function signup(data) {
  const res = await api.post('/api/auth/register', data);
  const { accessToken, user } = res.data;
  // Auto-login after register
  const auth = { token: accessToken, user };
  localStorage.setItem('auth', JSON.stringify(auth));
  return auth;
}

export async function login(credentials) {
  const payload = {
    identifier: credentials.username || credentials.email || credentials.identifier,
    password: credentials.password,
  };
  const res = await api.post('/api/auth/login', payload);
  const { accessToken, user } = res.data;
  const auth = { token: accessToken, user };
  localStorage.setItem('auth', JSON.stringify(auth));
  return auth;
}

export function logout() {
  localStorage.removeItem('auth');
}

export async function adminSignup(data) {
  const res = await api.post('/api/admin/auth/register', data);
  const { accessToken, user } = res.data;
  const auth = { token: accessToken, user };
  localStorage.setItem('auth', JSON.stringify(auth));
  return auth;
}

export async function adminLogin(credentials) {
  const res = await api.post('/api/admin/auth/login', credentials);
  const { accessToken, user } = res.data;
  const auth = { token: accessToken, user };
  localStorage.setItem('auth', JSON.stringify(auth));
  return auth;
}

export function getCurrentUser() {
  const auth = JSON.parse(localStorage.getItem('auth') || 'null');
  return auth?.user || null;
}

export function getToken() {
  const auth = JSON.parse(localStorage.getItem('auth') || 'null');
  return auth?.token || null;
}

export function isAuthenticated() {
  return !!getToken();
}

