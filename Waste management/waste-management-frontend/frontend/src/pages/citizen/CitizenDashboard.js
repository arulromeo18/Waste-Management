import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, StatCard } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import { scheduleApi } from '../../api/scheduleApi.js';
import { complaintApi } from '../../api/complaintApi.js';
import { rewardApi } from '../../api/rewardApi.js';
import { announcementApi } from '../../api/announcementApi.js';
import { useAuth } from '../../context/AuthContext.js';
import { formatTime, formatDateTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function CitizenDashboard() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [schedule, setSchedule] = useState(null);
  const [complaints, setComplaints] = useState([]);
  const [rewards, setRewards] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [scheduleRes, complaintsRes, rewardsRes, announcementsRes] = await Promise.all([
          scheduleApi.getMyZoneSchedule(),
          complaintApi.getMine(),
          rewardApi.getMyHistory(),
          announcementApi.getMyFeed(),
        ]);
        setSchedule(scheduleRes.data.data);
        setComplaints(complaintsRes.data.data || []);
        setRewards(rewardsRes.data.data || []);
        setAnnouncements((announcementsRes.data.data || []).slice(0, 3));
      } catch (err) {
        showToast(extractErrorMessage(err), 'error');
      } finally {
        setLoading(false);
      }
    }
    load();
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
  const pendingComplaints = complaints.filter((c) => c.status === 'PENDING' || c.status === 'IN_PROGRESS').length;

  return (
    <DashboardLayout>
      <PageHeader title={`Hi, ${user?.fullName?.split(' ')[0]}`} subtitle="Here's what's happening in your zone." />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Reward Points" value={totalPoints} icon="🏆" accent="brand" />
        <StatCard label="Active Complaints" value={pendingComplaints} icon="📮" accent="amber" />
        <StatCard label="Complaints Filed" value={complaints.length} icon="📝" accent="blue" />
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-ink-700">Your Collection Schedule</h3>
            <button className="btn-ghost text-xs" onClick={() => navigate('/citizen/schedule')}>
              View
            </button>
          </div>
          {schedule ? (
            <div>
              <p className="text-sm text-ink-600">
                Collected on{' '}
                <span className="font-semibold text-ink-900">
                  {(schedule.collectionDays || []).map((d) => d.slice(0, 3)).join(', ')}
                </span>
              </p>
              <p className="mt-1 text-sm text-ink-600">
                Between{' '}
                <span className="font-semibold text-ink-900">
                  {formatTime(schedule.startTime)} – {formatTime(schedule.endTime)}
                </span>
              </p>
            </div>
          ) : (
            <p className="text-sm text-ink-400">No active schedule for your zone yet.</p>
          )}
        </div>

        <div className="card p-5">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-ink-700">Recent Announcements</h3>
          </div>
          {announcements.length === 0 ? (
            <p className="text-sm text-ink-400">No announcements right now.</p>
          ) : (
            <ul className="space-y-3">
              {announcements.map((a) => (
                <li key={a.id} className="border-b border-ink-100 pb-3 last:border-0 last:pb-0">
                  <p className="text-sm font-semibold text-ink-800">{a.title}</p>
                  <p className="mt-0.5 text-xs text-ink-500">{a.message}</p>
                  <p className="mt-1 text-[11px] text-ink-400">{formatDateTime(a.createdAt)}</p>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className="mt-6 flex flex-wrap gap-3">
        <button className="btn-primary" onClick={() => navigate('/citizen/complaints/new')}>
          📝 Submit a Complaint
        </button>
        <button className="btn-secondary" onClick={() => navigate('/citizen/segregation-guide')}>
          ♻️ Segregation Guide
        </button>
      </div>
    </DashboardLayout>
  );
}
