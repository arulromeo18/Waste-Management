import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from './AuthLayout.js';
import { authApi } from '../../api/authApi.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function ResetPassword() {
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', otpCode: '', newPassword: '' });
  const [loading, setLoading] = useState(false);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    try {
      await authApi.resetPassword(form);
      showToast('Password reset successfully. Please log in.');
      navigate('/login');
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      title="Reset your password"
      subtitle="Enter the OTP sent to your email along with your new password."
      footer={
        <Link to="/login" className="font-semibold text-brand-700 hover:underline">
          Back to login
        </Link>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="label">Email address</label>
          <input type="email" required className="input" value={form.email} onChange={update('email')} />
        </div>
        <div>
          <label className="label">OTP code</label>
          <input required className="input" value={form.otpCode} onChange={update('otpCode')} />
        </div>
        <div>
          <label className="label">New password</label>
          <input
            type="password"
            required
            minLength={6}
            className="input"
            value={form.newPassword}
            onChange={update('newPassword')}
          />
        </div>
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Resetting…' : 'Reset password'}
        </button>
      </form>
    </AuthLayout>
  );
}
