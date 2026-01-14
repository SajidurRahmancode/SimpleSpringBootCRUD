import api from './api';

export async function listProducts(params = {}) {
  const res = await api.get('/api/products', { params });
  return res.data;
}

export async function getProduct(id) {
  const res = await api.get(`/api/products/${id}`);
  return res.data;
}

export async function createProduct(payload, options = {}) {
  const config = {};
  if (options.sellerIdentifier) {
    config.params = { sellerIdentifier: options.sellerIdentifier };
  }
  const res = await api.post('/api/products', payload, config);
  return res.data;
}

export async function createProductWithImage({ name, description, price, imageFile, stockQuantity, sellerIdentifier }) {
  const fd = new FormData();
  fd.append('name', name);
  if (description) fd.append('description', description);
  fd.append('price', price);
  if (stockQuantity != null) fd.append('stockQuantity', stockQuantity);
  if (imageFile) fd.append('image', imageFile);
  const config = {};
  if (sellerIdentifier) {
    config.params = { sellerIdentifier };
  }
  const res = await api.post('/api/products', fd, config);
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

export async function listSuppliedProducts(params = {}) {
  const res = await api.get('/api/products/supplied', { params });
  return res.data;
}


