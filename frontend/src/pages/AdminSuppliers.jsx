import React, { useCallback, useEffect, useState } from 'react';
import { fetchSupplierApplicationsForAdmin, reviewSupplierApplication } from '../services/supplier';

export default function AdminSuppliers() {
  const [apps, setApps] = useState({ content: [], number: 0, totalPages: 0 });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [statusFilter, setStatusFilter] = useState('PENDING');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notes, setNotes] = useState({});

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = { page, size };
      if (statusFilter) params.status = statusFilter;
      const data = await fetchSupplierApplicationsForAdmin(params);
      setApps(data);
    } catch (err) {
      const msg = err?.response?.data?.error || err?.response?.data?.message || 'Failed to load applications';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [page, size, statusFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const handleNoteChange = (id, value) => {
    setNotes(prev => ({ ...prev, [id]: value }));
  };

  const handleDecision = async (id, decision) => {
    try {
      await reviewSupplierApplication(id, { decision, adminNote: notes[id] || '' });
      setNotes(prev => ({ ...prev, [id]: '' }));
      load();
    } catch (err) {
      const msg = err?.response?.data?.error || err?.response?.data?.message || 'Unable to update application';
      alert(msg);
    }
  };

  return (
    <div>
      <header style={{ marginBottom: 24 }}>
        <h1>Supplier Approvals</h1>
        <p>Review and approve supplier applications submitted by sellers.</p>
      </header>

      <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
        <label>
          Status:
          <select value={statusFilter} onChange={e => { setPage(0); setStatusFilter(e.target.value); }} style={{ marginLeft: 8 }}>
            <option value="">All</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </label>
        <label>
          Size:
          <select value={size} onChange={e => { setPage(0); setSize(Number(e.target.value)); }} style={{ marginLeft: 8 }}>
            {[10, 20, 50].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
      </div>

      {error && <p style={{ color: 'red' }}>{error}</p>}
      {loading ? (
        <p>Loading...</p>
      ) : apps.content.length === 0 ? (
        <p>No applications found.</p>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#f4f4f4' }}>
                <th style={{ textAlign: 'left', padding: 8 }}>Submitted</th>
                <th style={{ textAlign: 'left', padding: 8 }}>Applicant</th>
                <th style={{ textAlign: 'left', padding: 8 }}>Business</th>
                <th style={{ textAlign: 'left', padding: 8 }}>Status</th>
                <th style={{ textAlign: 'left', padding: 8 }}>Admin Note</th>
                <th style={{ textAlign: 'left', padding: 8 }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {apps.content.map(app => (
                <tr key={app.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: 8 }}>{new Date(app.submittedAt).toLocaleString()}</td>
                  <td style={{ padding: 8 }}>{app.businessEmail}</td>
                  <td style={{ padding: 8 }}>{app.businessName}</td>
                  <td style={{ padding: 8 }}>{app.status}</td>
                  <td style={{ padding: 8, width: 220 }}>
                    <textarea
                      rows={2}
                      value={notes[app.id] ?? app.adminNote ?? ''}
                      onChange={e => handleNoteChange(app.id, e.target.value)}
                      style={{ width: '100%' }}
                    />
                  </td>
                  <td style={{ padding: 8, minWidth: 220 }}>
                    <button
                      disabled={app.status !== 'PENDING'}
                      onClick={() => handleDecision(app.id, 'APPROVE')}
                      style={{ marginRight: 8 }}
                    >
                      Approve
                    </button>
                    <button
                      disabled={app.status !== 'PENDING'}
                      onClick={() => handleDecision(app.id, 'REJECT')}
                    >
                      Reject
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div style={{ marginTop: 12, display: 'flex', alignItems: 'center', gap: 12 }}>
        <button disabled={page <= 0} onClick={() => setPage(p => Math.max(0, p - 1))}>Prev</button>
        <span>Page {apps.number + 1} / {apps.totalPages || 1}</span>
        <button disabled={apps.number + 1 >= (apps.totalPages || 1)} onClick={() => setPage(p => p + 1)}>Next</button>
      </div>
    </div>
  );
}
