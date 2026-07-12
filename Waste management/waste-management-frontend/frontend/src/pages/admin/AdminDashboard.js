import React, { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, StatCard } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import { dashboardApi, reportApi } from '../../api/dashboardApi.js';
import { downloadBlobResponse } from '../../utils/downloadFile.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

const PIE_COLORS = ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];

export default function AdminDashboard() {
  const { showToast } = useToast();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardApi
      .getStats()
      .then(({ data }) => setStats(data.data))
      .catch((err) => showToast(extractErrorMessage(err), 'error'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleDownloadPdf() {
    try {
      const response = await reportApi.downloadDashboardPdf();
      downloadBlobResponse(response, 'dashboard-report.pdf');
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  if (loading) {
    return (
      <DashboardLayout>
        <Loader />
      </DashboardLayout>
    );
  }

  const complaintsByZone = Object.entries(stats?.complaintsByZone || {}).map(([name, value]) => ({
    name,
    value,
  }));

  const complaintBreakdown = [
    { name: 'Pending', value: stats?.pendingComplaints || 0 },
    { name: 'In Progress', value: stats?.inProgressComplaints || 0 },
    { name: 'Resolved', value: stats?.resolvedComplaints || 0 },
    { name: 'Rejected', value: stats?.rejectedComplaints || 0 },
  ];

  return (
    <DashboardLayout>
      <PageHeader
        title="Dashboard"
        subtitle="City-wide waste management overview."
        action={
          <button className="btn-secondary" onClick={handleDownloadPdf}>
            ⬇ Download PDF report
          </button>
        }
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total Citizens" value={stats?.totalCitizens ?? 0} icon="🧑‍🤝‍🧑" accent="brand" />
        <StatCard label="Total Workers" value={stats?.totalWorkers ?? 0} icon="🧹" accent="blue" />
        <StatCard label="Total Vehicles" value={stats?.totalVehicles ?? 0} icon="🚛" accent="amber" />
        <StatCard label="Total Zones" value={stats?.totalZones ?? 0} icon="🗺️" accent="brand" />
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Pending Complaints" value={stats?.pendingComplaints ?? 0} icon="📮" accent="amber" />
        <StatCard label="Pending Penalties" value={stats?.pendingPenalties ?? 0} icon="⚠️" accent="red" />
        <StatCard
          label="Segregation Compliance"
          value={`${(stats?.segregationCompliancePercentage ?? 0).toFixed(1)}%`}
          icon="♻️"
          accent="brand"
        />
        <StatCard
          label="Collection Efficiency"
          value={`${(stats?.collectionEfficiencyPercentage ?? 0).toFixed(1)}%`}
          icon="✅"
          accent="blue"
        />
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h3 className="mb-4 text-sm font-semibold text-ink-700">Complaints by Status</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={complaintBreakdown}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="value" fill="#22c55e" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card p-5">
          <h3 className="mb-4 text-sm font-semibold text-ink-700">Complaints by Zone</h3>
          {complaintsByZone.length === 0 ? (
            <div className="flex h-64 items-center justify-center text-sm text-ink-400">
              No zone-wise complaint data yet.
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={complaintsByZone} dataKey="value" nameKey="name" outerRadius={90} label>
                  {complaintsByZone.map((_, idx) => (
                    <Cell key={idx} fill={PIE_COLORS[idx % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard
          label="Collections This Month"
          value={stats?.totalCollectionsThisMonth ?? 0}
          icon="🚚"
          accent="brand"
        />
        <StatCard
          label="Compliant Collections"
          value={stats?.compliantCollectionsThisMonth ?? 0}
          icon="✅"
          accent="blue"
        />
        <StatCard
          label="Reward Points Issued"
          value={stats?.totalRewardPointsIssued ?? 0}
          icon="🏆"
          accent="amber"
        />
      </div>
    </DashboardLayout>
  );
}
