import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { listProducts, deleteProduct } from '../services/products';
import { logout } from '../services/auth';

export default function Products() {
  const [pageData, setPageData] = useState({ content: [], number: 0, size: 20, totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [sort, setSort] = useState('name,asc');
  const navigate = useNavigate();

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await listProducts({ page, size, sort });
      setPageData(data);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, [page, size, sort]);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this product?')) return;
    try {
      await deleteProduct(id);
      await fetchData();
    } catch (e) {
      alert(e?.response?.data?.message || 'Delete failed');
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div>
      <h2>Products</h2>
      <div style={{ marginBottom: 12 }}>
        <Link to="/products/new">+ New Product</Link>
        <button style={{ marginLeft: 12 }} onClick={fetchData}>Refresh</button>
        <button style={{ marginLeft: 12 }} onClick={handleLogout}>Logout</button>
      </div>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {!loading && pageData.content.length === 0 && <p>No products yet.</p>}
      {!loading && pageData.content.length > 0 && (
        <table border="1" cellPadding="6" style={{ borderCollapse: 'collapse', width: '100%' }}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Image</th>
              <th>Name</th>
              <th>Description</th>
              <th>Price</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {pageData.content.map(p => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.imagePath ? <img src={p.imagePath} alt={p.name} style={{ width: 64, height: 64, objectFit: 'cover' }} /> : '-'}</td>
                <td>{p.name}</td>
                <td>{p.description}</td>
                <td>{Number(p.price).toFixed(2)}</td>
                <td>
                  <Link to={`/products/${p.id}/edit`}>Edit</Link>
                  <button style={{ marginLeft: 8 }} onClick={() => handleDelete(p.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <div style={{ marginTop: 12, display: 'flex', alignItems: 'center', gap: 12 }}>
        <button disabled={page <= 0} onClick={() => setPage(p => Math.max(0, p - 1))}>Prev</button>
        <span>Page {pageData.number + 1} / {pageData.totalPages || 1}</span>
        <button disabled={pageData.number + 1 >= (pageData.totalPages || 1)} onClick={() => setPage(p => p + 1)}>Next</button>
        <label>
          Size:
          <select value={size} onChange={e => setSize(Number(e.target.value))}>
            {[10,20,50,100].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
        <label>
          Sort:
          <select value={sort} onChange={e => setSort(e.target.value)}>
            <option value="name,asc">Name ↑</option>
            <option value="name,desc">Name ↓</option>
            <option value="price,asc">Price ↑</option>
            <option value="price,desc">Price ↓</option>
            <option value="id,asc">ID ↑</option>
            <option value="id,desc">ID ↓</option>
          </select>
        </label>
      </div>
    </div>
  );
}
