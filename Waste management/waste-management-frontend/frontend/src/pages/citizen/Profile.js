import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import { citizenProfileApi } from '../../api/citizenProfileApi.js';
import { useAuth } from '../../context/AuthContext.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function Profile() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    fullName: '',
    phone: '',
    address: '',
    houseNumber: '',
    landmark: '',
    pincode: '',
  });

  useEffect(() => {
    citizenProfileApi
      .getMyProfile()
      .then(({ data }) => {
        const p = data.data;
        setProfile(p);
        setForm({
          fullName: p.fullName || '',
          phone: p.phone || '',
          address: p.address || '',
          houseNumber: p.houseNumber || '',
          landmark: p.landmark || '',
          pincode: p.pincode || '',
        });
      })
      .catch((err) => showToast(extractErrorMessage(err), 'error'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await citizenProfileApi.updateMyProfile(form);
      showToast('Profile updated successfully');
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setSaving(false);
    }
  }

  return (
    <DashboardLayout>
      <PageHeader title="My Profile" subtitle="Update your contact and address details." />

      {loading ? (
        <Loader />
      ) : (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <div className="card p-5 lg:col-span-1">
            <div className="flex flex-col items-center text-center">
              <span className="flex h-16 w-16 items-center justify-center rounded-full bg-ink-800 text-2xl font-bold text-white">
                {user?.fullName?.charAt(0)?.toUpperCase()}
              </span>
              <h3 className="mt-3 font-semibold text-ink-900">{user?.fullName}</h3>
              <p className="text-sm text-ink-500">{user?.email}</p>
              <div className="mt-4 w-full space-y-2 border-t border-ink-100 pt-4 text-left text-sm">
                <div className="flex justify-between">
                  <span className="text-ink-500">Reward points</span>
                  <span className="font-semibold text-brand-700">{profile?.rewardPoints ?? 0}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-ink-500">Complaints filed</span>
                  <span className="font-semibold text-ink-800">{profile?.totalComplaintsFiled ?? 0}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-ink-500">Zone</span>
                  <span className="font-semibold text-ink-800">{profile?.zoneName ?? '—'}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="card p-5 lg:col-span-2">
            <h3 className="mb-4 text-sm font-semibold text-ink-700">Edit Details</h3>
            <form onSubmit={handleSave} className="space-y-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="label">Full name</label>
                  <input
                    className="input"
                    value={form.fullName}
                    onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                  />
                </div>
                <div>
                  <label className="label">Phone</label>
                  <input
                    className="input"
                    value={form.phone}
                    onChange={(e) => setForm({ ...form, phone: e.target.value })}
                  />
                </div>
              </div>
              <div>
                <label className="label">Address</label>
                <input
                  className="input"
                  value={form.address}
                  onChange={(e) => setForm({ ...form, address: e.target.value })}
                />
              </div>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <div>
                  <label className="label">House number</label>
                  <input
                    className="input"
                    value={form.houseNumber}
                    onChange={(e) => setForm({ ...form, houseNumber: e.target.value })}
                  />
                </div>
                <div>
                  <label className="label">Landmark</label>
                  <input
                    className="input"
                    value={form.landmark}
                    onChange={(e) => setForm({ ...form, landmark: e.target.value })}
                  />
                </div>
                <div>
                  <label className="label">Pincode</label>
                  <input
                    className="input"
                    value={form.pincode}
                    onChange={(e) => setForm({ ...form, pincode: e.target.value })}
                  />
                </div>
              </div>
              <button type="submit" disabled={saving} className="btn-primary">
                {saving ? 'Saving…' : 'Save Changes'}
              </button>
            </form>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}
