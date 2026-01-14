import api from './api';

export async function signup(data) {
  const res = await api.post('/api/auth/register', data);
  const { accessToken, user } = res.data;
  // Auto-login after register
  const auth = { token: accessToken, user };
  localStorage.setItem('auth', JSON.stringify(auth));
  broadcastAuthChange();
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
  broadcastAuthChange();
  return auth;
}

export function logout() {
  localStorage.removeItem('auth');
  broadcastAuthChange();
}

export async function adminSignup(data) {
  const res = await api.post('/api/admin/auth/register', data);
  const { accessToken, user } = res.data;
  const auth = { token: accessToken, user };
  localStorage.setItem('auth', JSON.stringify(auth));
  broadcastAuthChange();
  return auth;
}

export async function adminLogin(credentials) {
  const res = await api.post('/api/admin/auth/login', credentials);
  const { accessToken, user } = res.data;
  const auth = { token: accessToken, user };
  localStorage.setItem('auth', JSON.stringify(auth));
  broadcastAuthChange();
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

export async function refreshCurrentUser() {
  const token = getToken();
  if (!token) return null;
  try {
    const res = await api.get('/api/auth/me');
    const auth = JSON.parse(localStorage.getItem('auth') || 'null');
    if (auth?.token) {
      const next = { token: auth.token, user: res.data };
      localStorage.setItem('auth', JSON.stringify(next));
      broadcastAuthChange();
    }
    return res.data;
  } catch (err) {
    if (err?.response?.status === 401) {
      logout();
    }
    throw err;
  }
}

function broadcastAuthChange() {
  window.dispatchEvent(new Event('auth-changed'));
}

