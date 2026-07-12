import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, StatCard } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import { scheduleApi } from '../../api/scheduleApi.js';
import { collectionRecordApi } from '../../api/collectionRecordApi.js';
import { useAuth } from '../../context/AuthContext.js';
import { formatTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function WorkerDashboard() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [todaySchedules, setTodaySchedules] = useState([]);
  const [myRecords, setMyRecords] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [scheduleRes, recordsRes] = await Promise.all([
          scheduleApi.getMySchedule(true),
          collectionRecordApi.getMyRecords(),
        ]);
        setTodaySchedules(scheduleRes.data.data || []);
        setMyRecords(recordsRes.data.data || []);
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

  const compliantCount = myRecords.filter((r) => r.segregationCompliant).length;
  const nonCompliantCount = myRecords.length - compliantCount;

  return (
    <DashboardLayout>
      <PageHeader title={`Welcome, ${user?.fullName?.split(' ')[0]}`} subtitle="Here's your work summary." />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Scheduled Today" value={todaySchedules.length} icon="📅" accent="brand" />
        <StatCard label="Total Collections Logged" value={myRecords.length} icon="🚚" accent="blue" />
        <StatCard label="Compliant Pickups" value={compliantCount} icon="✅" accent="brand" />
        <StatCard label="Flagged Pickups" value={nonCompliantCount} icon="⚠️" accent="red" />
      </div>

      <div className="mt-6 card p-5">
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-ink-700">Today's Schedule</h3>
          <button className="btn-ghost text-xs" onClick={() => navigate('/worker/today-schedule')}>
            View all
          </button>
        </div>
        {todaySchedules.length === 0 ? (
          <p className="py-6 text-center text-sm text-ink-400">No collection scheduled for today.</p>
        ) : (
          <ul className="divide-y divide-ink-100">
            {todaySchedules.map((s) => (
              <li key={s.id} className="flex items-center justify-between py-3 text-sm">
                <div>
                  <p className="font-medium text-ink-800">{s.zoneName}</p>
                  <p className="text-xs text-ink-400">
                    {formatTime(s.startTime)} – {formatTime(s.endTime)}
                  </p>
                </div>
                <button className="btn-primary text-xs" onClick={() => navigate('/worker/upload')}>
                  Log Pickup
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </DashboardLayout>
  );
}
