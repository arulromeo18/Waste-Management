import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from './AuthLayout.js';
import { authApi } from '../../api/authApi.js';
import { useAuth } from '../../context/AuthContext.js';
import { roleHome } from '../../routes/ProtectedRoute.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

const initialForm = {
  fullName: '',
  email: '',
  password: '',
  phone: '',
  zoneId: '',
  address: '',
  houseNumber: '',
  landmark: '',
  pincode: '',
};

export default function Register() {
  const { register } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    authApi
      .getActiveZonesPublic()
      .then(({ data }) => setZones(data.data || []))
      .catch(() => setZones([]));
  }, []);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    try {
      const sessionUser = await register(form);
      showToast('Registration successful! Welcome to CleanCity.');
      navigate(roleHome(sessionUser.role));
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      title="Create your citizen account"
      subtitle="Register to view your collection schedule, submit complaints, and earn rewards."
      footer={
        <>
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-brand-700 hover:underline">
            Log in
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="label">Full name</label>
            <input required className="input" value={form.fullName} onChange={update('fullName')} />
          </div>
          <div>
            <label className="label">Phone</label>
            <input required className="input" value={form.phone} onChange={update('phone')} />
          </div>
        </div>
        <div>
          <label className="label">Email address</label>
          <input type="email" required className="input" value={form.email} onChange={update('email')} />
        </div>
        <div>
          <label className="label">Password</label>
          <input
            type="password"
            required
            minLength={6}
            className="input"
            value={form.password}
            onChange={update('password')}
          />
        </div>
        <div>
          <label className="label">Zone</label>
          <select required className="input" value={form.zoneId} onChange={update('zoneId')}>
            <option value="">Select your zone</option>
            {zones.map((z) => (
              <option key={z.id} value={z.id}>
                {z.zoneName} ({z.zoneCode})
              </option>
            ))}
          </select>
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="label">House number</label>
            <input className="input" value={form.houseNumber} onChange={update('houseNumber')} />
          </div>
          <div>
            <label className="label">Pincode</label>
            <input className="input" value={form.pincode} onChange={update('pincode')} />
          </div>
        </div>
        <div>
          <label className="label">Address</label>
          <input required className="input" value={form.address} onChange={update('address')} />
        </div>
        <div>
          <label className="label">Landmark (optional)</label>
          <input className="input" value={form.landmark} onChange={update('landmark')} />
        </div>
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Creating account…' : 'Create account'}
        </button>
      </form>
    </AuthLayout>
  );
}
