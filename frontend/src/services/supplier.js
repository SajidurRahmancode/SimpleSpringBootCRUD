import api from './api';

export async function fetchSupplierDashboard() {
  const res = await api.get('/api/suppliers/dashboard');
  return res.data;
}

export async function submitSupplierApplication(payload) {
  const res = await api.post('/api/suppliers/applications', payload);
  return res.data;
}

export async function fetchSupplierApplicationsForAdmin(params = {}) {
  const res = await api.get('/api/admin/suppliers/applications', { params });
  return res.data;
}

export async function reviewSupplierApplication(id, payload) {
  const res = await api.patch(`/api/admin/suppliers/applications/${id}`, payload);
  return res.data;
}
