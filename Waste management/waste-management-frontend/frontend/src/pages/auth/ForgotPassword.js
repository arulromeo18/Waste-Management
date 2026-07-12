import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from './AuthLayout.js';
import { authApi } from '../../api/authApi.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function ForgotPassword() {
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    try {
      await authApi.forgotPassword({ email });
      setSent(true);
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      title="Forgot your password?"
      subtitle="Enter your email and we'll send you a one-time code to reset it."
      footer={
        <Link to="/login" className="font-semibold text-brand-700 hover:underline">
          Back to login
        </Link>
      }
    >
      {sent ? (
        <div className="space-y-4 text-center">
          <p className="text-sm text-ink-600">
            If an account exists for <span className="font-semibold">{email}</span>, a reset code has
            been sent. Check your inbox for the OTP.
          </p>
          <button onClick={() => navigate('/reset-password')} className="btn-primary w-full">
            Enter reset code
          </button>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label">Email address</label>
            <input
              type="email"
              required
              className="input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? 'Sending…' : 'Send reset code'}
          </button>
        </form>
      )}
    </AuthLayout>
  );
}
