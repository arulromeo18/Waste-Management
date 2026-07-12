import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import { scheduleApi } from '../../api/scheduleApi.js';
import { formatTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function TodaySchedule() {
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    scheduleApi
      .getMySchedule(true)
      .then(({ data }) => setSchedules(data.data || []))
      .catch((err) => showToast(extractErrorMessage(err), 'error'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const todayName = new Date()
    .toLocaleDateString('en-US', { weekday: 'long' })
    .toUpperCase();

  return (
    <DashboardLayout>
      <PageHeader title="Today's Schedule" subtitle={`Your collection stops for ${todayName}.`} />

      {loading ? (
        <Loader />
      ) : schedules.length === 0 ? (
        <EmptyState title="No collection scheduled today" subtitle="Enjoy your day off!" icon="📅" />
      ) : (
        <div className="space-y-3">
          {schedules.map((s) => (
            <div key={s.id} className="card flex items-center justify-between p-5">
              <div>
                <h3 className="font-semibold text-ink-900">{s.zoneName}</h3>
                <p className="mt-1 text-sm text-ink-500">
                  {formatTime(s.startTime)} – {formatTime(s.endTime)}
                </p>
                <p className="mt-1 text-xs text-ink-400">Vehicle: {s.vehicleNumber || 'Not assigned'}</p>
              </div>
              <button className="btn-primary" onClick={() => navigate('/worker/upload')}>
                Log Pickup
              </button>
            </div>
          ))}
        </div>
      )}
    </DashboardLayout>
  );
}
