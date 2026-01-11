import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createProductWithImage, getProduct, updateProductWithImage } from '../services/products';

export default function ProductForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('0');
  const [stockQuantity, setStockQuantity] = useState('0');
  const [imageFile, setImageFile] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!isEdit) return;
      setLoading(true);
      try {
        const p = await getProduct(id);
        setName(p.name || '');
        setDescription(p.description || '');
        setPrice(String(p.price ?? '0'));
        setStockQuantity(String(p.stockQuantity ?? '0'));
      } catch (e) {
        setError(e?.response?.data?.message || 'Failed to load product');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id, isEdit]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const payload = { name, description, price: Number(price), stockQuantity: Number(stockQuantity), imageFile };
      if (isEdit) await updateProductWithImage(id, payload);
      else await createProductWithImage(payload);
      navigate('/products');
    } catch (e) {
      setError(e?.response?.data?.message || 'Save failed');
    }
  };

  return (
    <div>
      <h2>{isEdit ? 'Edit Product' : 'New Product'}</h2>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit} style={{ maxWidth: 500 }}>
        <div style={{ marginBottom: 12 }}>
          <label>Name</label><br />
          <input value={name} onChange={e => setName(e.target.value)} required style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Description</label><br />
          <textarea value={description} onChange={e => setDescription(e.target.value)} rows={4} style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Price</label><br />
          <input type="number" step="0.01" min="0" value={price} onChange={e => setPrice(e.target.value)} style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Stock Quantity</label><br />
          <input type="number" min="0" value={stockQuantity} onChange={e => setStockQuantity(e.target.value)} style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Image</label><br />
          <input type="file" accept="image/*" onChange={e => setImageFile(e.target.files?.[0] || null)} />
        </div>
        <button type="submit">Save</button>
      </form>
    </div>
  );
}
