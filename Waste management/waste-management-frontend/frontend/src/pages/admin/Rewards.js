import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import { userApi } from '../../api/userApi.js';
import { rewardApi } from '../../api/rewardApi.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function Rewards() {
  const { showToast } = useToast();
  const [citizens, setCitizens] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const [form, setForm] = useState({ citizenId: '', points: '', reason: '' });
  const [awarding, setAwarding] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const { data } = await userApi.getAllCitizens();
      const list = (data.data || []).sort((a, b) => (b.rewardPoints || 0) - (a.rewardPoints || 0));
      setCitizens(list);
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filtered = citizens.filter((c) => c.fullName.toLowerCase().includes(search.toLowerCase()));

  async function handleAward(e) {
    e.preventDefault();
    if (!form.citizenId || !form.points) {
      showToast('Select a citizen and enter points', 'error');
      return;
    }
    setAwarding(true);
    try {
      await rewardApi.awardBonus({ ...form, points: Number(form.points) });
      showToast('Bonus points awarded successfully');
      setForm({ citizenId: '', points: '', reason: '' });
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setAwarding(false);
    }
  }

  return (
    <DashboardLayout>
      <PageHeader title="Rewards" subtitle="Zone leaderboard and manual bonus point awards." />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="card p-5 lg:col-span-1">
          <h3 className="mb-4 text-sm font-semibold text-ink-700">Award Bonus Points</h3>
          <form onSubmit={handleAward} className="space-y-4">
            <div>
              <label className="label">Citizen</label>
              <select
                required
                className="input"
                value={form.citizenId}
                onChange={(e) => setForm({ ...form, citizenId: e.target.value })}
              >
                <option value="">Select citizen</option>
                {citizens.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.fullName}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Points</label>
              <input
                type="number"
                required
                min={1}
                className="input"
                value={form.points}
                onChange={(e) => setForm({ ...form, points: e.target.value })}
              />
            </div>
            <div>
              <label className="label">Reason</label>
              <input
                required
                className="input"
                placeholder="e.g. Community cleanup participation"
                value={form.reason}
                onChange={(e) => setForm({ ...form, reason: e.target.value })}
              />
            </div>
            <button type="submit" disabled={awarding} className="btn-primary w-full">
              {awarding ? 'Awarding…' : 'Award Points'}
            </button>
          </form>
        </div>

        <div className="lg:col-span-2">
          <h3 className="mb-3 text-sm font-semibold text-ink-700">Citizen Leaderboard</h3>
          <input
            className="input mb-3 sm:max-w-xs"
            placeholder="Search citizens…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {loading ? (
            <Loader />
          ) : filtered.length === 0 ? (
            <EmptyState title="No citizens found" icon="🏆" />
          ) : (
            <div className="table-shell">
              <table className="table-base">
                <thead>
                  <tr>
                    <th>Rank</th>
                    <th>Citizen</th>
                    <th>Reward Points</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((c, idx) => (
                    <tr key={c.id}>
                      <td className="font-semibold text-ink-500">#{idx + 1}</td>
                      <td className="font-medium text-ink-900">{c.fullName}</td>
                      <td className="font-semibold text-brand-700">{c.rewardPoints ?? 0} pts</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
}
