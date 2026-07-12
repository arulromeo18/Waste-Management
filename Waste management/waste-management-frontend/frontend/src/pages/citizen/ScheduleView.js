import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import { scheduleApi } from '../../api/scheduleApi.js';
import { DAYS_OF_WEEK } from '../../utils/constants.js';
import { formatTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function ScheduleView() {
  const { showToast } = useToast();
  const [schedule, setSchedule] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    scheduleApi
      .getMyZoneSchedule()
      .then(({ data }) => setSchedule(data.data))
      .catch((err) => showToast(extractErrorMessage(err), 'error'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <DashboardLayout>
      <PageHeader title="Collection Schedule" subtitle="Your zone's recurring waste collection schedule." />

      {loading ? (
        <Loader />
      ) : !schedule ? (
        <EmptyState
          title="No active schedule yet"
          subtitle="Your zone's collection schedule hasn't been published yet. Check back soon."
          icon="📅"
        />
      ) : (
        <div className="card p-6">
          <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
            <div>
              <p className="text-sm text-ink-500">Collection window</p>
              <p className="text-2xl font-bold text-ink-900">
                {formatTime(schedule.startTime)} – {formatTime(schedule.endTime)}
              </p>
            </div>
            <div>
              <p className="text-sm text-ink-500">Assigned vehicle</p>
              <p className="font-semibold text-ink-800">{schedule.vehicleNumber || 'Not assigned yet'}</p>
            </div>
          </div>

          <p className="mb-3 text-sm font-semibold text-ink-700">Collection Days</p>
          <div className="grid grid-cols-7 gap-2">
            {DAYS_OF_WEEK.map((day) => {
              const active = (schedule.collectionDays || []).includes(day);
              return (
                <div
                  key={day}
                  className={`rounded-lg py-3 text-center text-xs font-semibold ${
                    active ? 'bg-brand-600 text-white' : 'bg-ink-100 text-ink-400'
                  }`}
                >
                  {day.slice(0, 3)}
                </div>
              );
            })}
          </div>

          <div className="mt-6 rounded-lg bg-amber-50 p-4 text-sm text-amber-800">
            💡 Please keep your waste segregated (wet/dry) and out for collection before the window starts.
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}
