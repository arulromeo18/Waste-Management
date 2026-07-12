import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, StatCard, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import { rewardApi } from '../../api/rewardApi.js';
import { penaltyApi } from '../../api/penaltyApi.js';
import { formatDateTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function CitizenRewards() {
  const { showToast } = useToast();
  const [rewards, setRewards] = useState([]);
  const [penalties, setPenalties] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([rewardApi.getMyHistory(), penaltyApi.getMine()])
      .then(([rewardsRes, penaltiesRes]) => {
        setRewards(rewardsRes.data.data || []);
        setPenalties(penaltiesRes.data.data || []);
      })
      .catch((err) => showToast(extractErrorMessage(err), 'error'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) {
    return (
      <DashboardLayout>
        <Loader />
      </DashboardLayout>
    );
  }

  const totalPoints = rewards.reduce((sum, r) => sum + (r.points || 0), 0);
  const pendingFines = penalties
    .filter((p) => p.status === 'PENDING')
    .reduce((sum, p) => sum + (p.fineAmount || 0), 0);

  return (
    <DashboardLayout>
      <PageHeader title="Rewards & Penalties" subtitle="Your reward point history and any fines issued." />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <StatCard label="Total Reward Points" value={totalPoints} icon="🏆" accent="brand" />
        <StatCard label="Pending Fines" value={`₹${pendingFines.toFixed(2)}`} icon="⚠️" accent="red" />
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div>
          <h3 className="mb-3 text-sm font-semibold text-ink-700">Reward History</h3>
          {rewards.length === 0 ? (
            <EmptyState title="No rewards yet" subtitle="Keep segregating waste properly!" icon="🏆" />
          ) : (
            <div className="space-y-2">
              {rewards.map((r) => (
                <div key={r.id} className="card flex items-center justify-between p-4">
                  <div>
                    <p className="text-sm font-medium text-ink-800">{r.reason}</p>
                    <p className="text-xs text-ink-400">{formatDateTime(r.createdAt)}</p>
                  </div>
                  <span className="font-semibold text-brand-700">+{r.points} pts</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div>
          <h3 className="mb-3 text-sm font-semibold text-ink-700">Penalty History</h3>
          {penalties.length === 0 ? (
            <EmptyState title="No penalties issued" subtitle="Great job staying compliant." icon="✅" />
          ) : (
            <div className="space-y-2">
              {penalties.map((p) => (
                <div key={p.id} className="card p-4">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-medium text-ink-800">{p.reason}</p>
                    <Badge status={p.status} />
                  </div>
                  <div className="mt-2 flex items-center justify-between text-xs text-ink-400">
                    <span>{formatDateTime(p.createdAt)}</span>
                    <span className="font-semibold text-red-600">₹{Number(p.fineAmount).toFixed(2)}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
}
