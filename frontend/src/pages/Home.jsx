import React, { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getCurrentUser, logout } from '../services/auth';
import { fetchSupplierDashboard, submitSupplierApplication } from '../services/supplier';

export default function Home() {
  const [user, setUser] = useState(() => getCurrentUser());
  const [dashboard, setDashboard] = useState(null);
  const [dashLoading, setDashLoading] = useState(true);
  const [dashError, setDashError] = useState('');
  const [applyError, setApplyError] = useState('');
  const [applySuccess, setApplySuccess] = useState('');
  const [applyLoading, setApplyLoading] = useState(false);
  const [formValues, setFormValues] = useState({
    businessName: user?.supplierProfile || '',
    businessEmail: user?.email || '',
    businessPhone: '',
    website: '',
    message: ''
  });

  useEffect(() => {
    const handler = () => setUser(getCurrentUser());
    window.addEventListener('auth-changed', handler);
    return () => window.removeEventListener('auth-changed', handler);
  }, []);

  useEffect(() => {
    setFormValues(prev => ({
      ...prev,
      businessName: prev.businessName || user?.supplierProfile || '',
      businessEmail: user?.email || prev.businessEmail,
    }));
  }, [user]);

  const loadDashboard = useCallback(async () => {
    if (!user) return;
    setDashLoading(true);
    setDashError('');
    try {
      const data = await fetchSupplierDashboard();
      setDashboard(data);
    } catch (err) {
      const msg = err?.response?.data?.error || err?.response?.data?.message || 'Failed to load supplier dashboard';
      setDashError(msg);
    } finally {
      setDashLoading(false);
    }
  }, [user]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const handleApply = async (e) => {
    e.preventDefault();
    setApplyError('');
    setApplySuccess('');
    setApplyLoading(true);
    try {
      await submitSupplierApplication(formValues);
      setApplySuccess('Application submitted! You will see updates below.');
      setFormValues(prev => ({ ...prev, message: '' }));
      await loadDashboard();
    } catch (err) {
      const msg = err?.response?.data?.error || err?.response?.data?.message || 'Unable to submit application';
      setApplyError(msg);
    } finally {
      setApplyLoading(false);
    }
  };

  const severityColor = (type) => {
    switch (type) {
      case 'success':
        return '#e6ffed';
      case 'warning':
        return '#fff4e5';
      default:
        return '#e8f0ff';
    }
  };

  return (
    <div style={{ fontFamily: 'sans-serif', padding: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ marginBottom: 8 }}>Welcome {user?.username || user?.email}</h1>
          <p style={{ margin: 0 }}>Role: {user?.role}</p>
        </div>
        <button onClick={handleLogout}>Logout</button>
      </div>

      <section style={{ marginTop: 24, border: '1px solid #ddd', borderRadius: 8, padding: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <div>
            <h2 style={{ margin: 0 }}>Supplier Notifications</h2>
            <small>Track approvals and requests in one place.</small>
          </div>
          {dashboard?.supplier && (
            <Link to="/supplier" style={{ fontWeight: 'bold' }}>Go to Supplier Hub →</Link>
          )}
        </div>
        {dashLoading && <p>Loading supplier dashboard...</p>}
        {dashError && <p style={{ color: 'red' }}>{dashError}</p>}
        {!dashLoading && dashboard && (
          <>
            <div style={{ display: 'grid', gap: 8, marginBottom: 16 }}>
              {(dashboard.alerts || []).map((alert, idx) => (
                <div key={`${alert.severity}-${idx}`} style={{ background: severityColor(alert.severity), padding: 12, borderRadius: 6 }}>
                  <strong style={{ textTransform: 'uppercase', marginRight: 6 }}>{alert.severity}</strong>
                  {alert.message}
                </div>
              ))}
              {(!dashboard.alerts || dashboard.alerts.length === 0) && (
                <div style={{ background: '#f7f7f7', padding: 12, borderRadius: 6 }}>
                  No alerts yet. Submit an application to get started.
                </div>
              )}
            </div>

            <div style={{ marginBottom: 24 }}>
              <h3 style={{ marginBottom: 8 }}>Your Requests</h3>
              {(dashboard.applications || []).length === 0 ? (
                <p>No supplier requests yet.</p>
              ) : (
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ background: '#f4f4f4' }}>
                        <th style={{ textAlign: 'left', padding: 8 }}>Submitted</th>
                        <th style={{ textAlign: 'left', padding: 8 }}>Business</th>
                        <th style={{ textAlign: 'left', padding: 8 }}>Status</th>
                        <th style={{ textAlign: 'left', padding: 8 }}>Admin Note</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dashboard.applications.map(app => (
                        <tr key={app.id} style={{ borderBottom: '1px solid #eee' }}>
                          <td style={{ padding: 8 }}>{new Date(app.submittedAt).toLocaleString()}</td>
                          <td style={{ padding: 8 }}>{app.businessName}</td>
                          <td style={{ padding: 8 }}>{app.status}</td>
                          <td style={{ padding: 8 }}>{app.adminNote || '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            <div style={{ borderTop: '1px solid #eee', paddingTop: 16 }}>
              <h3 style={{ marginBottom: 8 }}>Apply to Become a Supplier</h3>
              {applySuccess && <p style={{ color: 'green' }}>{applySuccess}</p>}
              {applyError && <p style={{ color: 'red' }}>{applyError}</p>}
              {dashboard.canApply ? (
                <form onSubmit={handleApply} style={{ display: 'grid', gap: 12, maxWidth: 500 }}>
                  <label style={{ display: 'grid', gap: 4 }}>
                    Business Name
                    <input name="businessName" value={formValues.businessName} onChange={handleChange} required />
                  </label>
                  <label style={{ display: 'grid', gap: 4 }}>
                    Business Email
                    <input type="email" name="businessEmail" value={formValues.businessEmail} onChange={handleChange} required />
                  </label>
                  <label style={{ display: 'grid', gap: 4 }}>
                    Business Phone
                    <input name="businessPhone" value={formValues.businessPhone} onChange={handleChange} />
                  </label>
                  <label style={{ display: 'grid', gap: 4 }}>
                    Website
                    <input name="website" value={formValues.website} onChange={handleChange} placeholder="https://" />
                  </label>
                  <label style={{ display: 'grid', gap: 4 }}>
                    Message
                    <textarea name="message" rows={3} value={formValues.message} onChange={handleChange} placeholder="Tell the admin about your catalog..." />
                  </label>
                  <button type="submit" disabled={applyLoading}>{applyLoading ? 'Submitting...' : 'Submit application'}</button>
                </form>
              ) : (
                <p style={{ fontStyle: 'italic' }}>You already have an application under review or are approved.</p>
              )}
            </div>
          </>
        )}
      </section>
    </div>
  );
}

