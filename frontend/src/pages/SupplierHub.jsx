import React, { useCallback, useEffect, useState } from 'react';
import { createProductWithImage, listSuppliedProducts } from '../services/products';

export default function SupplierHub() {
  const [formValues, setFormValues] = useState({
    sellerIdentifier: '',
    name: '',
    description: '',
    price: '0',
    stockQuantity: '0',
    imageFile: null,
  });
  const [submitError, setSubmitError] = useState('');
  const [submitSuccess, setSubmitSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [pageData, setPageData] = useState({ content: [], number: 0, totalPages: 0 });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [loading, setLoading] = useState(true);

  const fetchSupplied = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listSuppliedProducts({ page, size });
      setPageData(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => {
    fetchSupplied();
  }, [fetchSupplied]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const handleFile = (e) => {
    setFormValues(prev => ({ ...prev, imageFile: e.target.files?.[0] || null }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError('');
    setSubmitSuccess('');
    setSubmitting(true);
    try {
      await createProductWithImage({
        name: formValues.name,
        description: formValues.description,
        price: Number(formValues.price),
        stockQuantity: Number(formValues.stockQuantity),
        imageFile: formValues.imageFile,
        sellerIdentifier: formValues.sellerIdentifier,
      });
      setSubmitSuccess('Product sent to seller successfully.');
      setFormValues(prev => ({ ...prev, name: '', description: '', price: '0', stockQuantity: '0', imageFile: null }));
      fetchSupplied();
    } catch (err) {
      const msg = err?.response?.data?.error || err?.response?.data?.message || 'Unable to create product';
      setSubmitError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <header style={{ marginBottom: 24 }}>
        <h1>Supplier Hub</h1>
        <p>Provide inventory directly to sellers. Use their username, email, or ID to assign ownership.</p>
      </header>

      <section style={{ marginBottom: 32 }}>
        <h2 style={{ marginBottom: 8 }}>Supply a Product</h2>
        {submitSuccess && <p style={{ color: 'green' }}>{submitSuccess}</p>}
        {submitError && <p style={{ color: 'red' }}>{submitError}</p>}
        <form onSubmit={handleSubmit} style={{ display: 'grid', gap: 12, maxWidth: 600 }}>
          <label style={{ display: 'grid', gap: 4 }}>
            Seller Identifier
            <input
              name="sellerIdentifier"
              value={formValues.sellerIdentifier}
              onChange={handleChange}
              required
              placeholder="username or email"
            />
          </label>
          <label style={{ display: 'grid', gap: 4 }}>
            Name
            <input name="name" value={formValues.name} onChange={handleChange} required />
          </label>
          <label style={{ display: 'grid', gap: 4 }}>
            Description
            <textarea name="description" rows={3} value={formValues.description} onChange={handleChange} />
          </label>
          <label style={{ display: 'grid', gap: 4 }}>
            Price
            <input type="number" min="0" step="0.01" name="price" value={formValues.price} onChange={handleChange} />
          </label>
          <label style={{ display: 'grid', gap: 4 }}>
            Quantity
            <input type="number" min="0" name="stockQuantity" value={formValues.stockQuantity} onChange={handleChange} />
          </label>
          <label style={{ display: 'grid', gap: 4 }}>
            Image (optional)
            <input type="file" accept="image/*" onChange={handleFile} />
          </label>
          <button type="submit" disabled={submitting}>{submitting ? 'Sending...' : 'Send to seller'}</button>
        </form>
      </section>

      <section>
        <h2 style={{ marginBottom: 8 }}>Recently Supplied Products</h2>
        {loading ? (
          <p>Loading...</p>
        ) : pageData.content.length === 0 ? (
          <p>No supplied products yet.</p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#f4f4f4' }}>
                  <th style={{ textAlign: 'left', padding: 8 }}>ID</th>
                  <th style={{ textAlign: 'left', padding: 8 }}>Name</th>
                  <th style={{ textAlign: 'left', padding: 8 }}>Seller</th>
                  <th style={{ textAlign: 'left', padding: 8 }}>Price</th>
                  <th style={{ textAlign: 'left', padding: 8 }}>Qty</th>
                  <th style={{ textAlign: 'left', padding: 8 }}>Updated</th>
                </tr>
              </thead>
              <tbody>
                {pageData.content.map(product => (
                  <tr key={product.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: 8 }}>{product.id}</td>
                    <td style={{ padding: 8 }}>{product.name}</td>
                    <td style={{ padding: 8 }}>{product.ownerUsername || product.ownerId}</td>
                    <td style={{ padding: 8 }}>{Number(product.price).toFixed(2)}</td>
                    <td style={{ padding: 8 }}>{product.stockQuantity}</td>
                    <td style={{ padding: 8 }}>{product.updatedAt ? new Date(product.updatedAt).toLocaleDateString() : '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <div style={{ marginTop: 12, display: 'flex', alignItems: 'center', gap: 12 }}>
          <button disabled={page <= 0} onClick={() => setPage(p => Math.max(0, p - 1))}>Prev</button>
          <span>Page {pageData.number + 1} / {pageData.totalPages || 1}</span>
          <button disabled={pageData.number + 1 >= (pageData.totalPages || 1)} onClick={() => setPage(p => p + 1)}>Next</button>
          <label>
            Size:
            <select value={size} onChange={e => setSize(Number(e.target.value))} style={{ marginLeft: 4 }}>
              {[5, 10, 20, 50].map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </label>
        </div>
      </section>
    </div>
  );
}
