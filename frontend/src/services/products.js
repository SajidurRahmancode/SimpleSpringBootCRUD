import api from './api';

export async function listProducts(params = {}) {
  const res = await api.get('/api/products', { params });
  return res.data;
}

export async function getProduct(id) {
  const res = await api.get(`/api/products/${id}`);
  return res.data;
}

export async function createProduct(payload) {
  const res = await api.post('/api/products', payload);
  return res.data;
}

export async function createProductWithImage({ name, description, price, imageFile, stockQuantity }) {
  const fd = new FormData();
  fd.append('name', name);
  if (description) fd.append('description', description);
  fd.append('price', price);
  if (stockQuantity != null) fd.append('stockQuantity', stockQuantity);
  if (imageFile) fd.append('image', imageFile);
  const res = await api.post('/api/products', fd);
  return res.data;
}

export async function updateProduct(id, payload) {
  const res = await api.put(`/api/products/${id}`, payload);
  return res.data;
}

export async function updateProductWithImage(id, { name, description, price, imageFile, stockQuantity }) {
  const fd = new FormData();
  fd.append('name', name);
  if (description) fd.append('description', description);
  fd.append('price', price);
  if (stockQuantity != null) fd.append('stockQuantity', stockQuantity);
  if (imageFile) fd.append('image', imageFile);
  const res = await api.put(`/api/products/${id}`, fd);
  return res.data;
}

export async function deleteProduct(id) {
  await api.delete(`/api/products/${id}`);
}


