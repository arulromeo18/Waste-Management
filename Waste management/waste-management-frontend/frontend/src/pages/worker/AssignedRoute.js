import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import { scheduleApi } from '../../api/scheduleApi.js';
import { collectionRecordApi } from '../../api/collectionRecordApi.js';
import { formatTime, formatDate } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function AssignedRoute() {
  const { showToast } = useToast();
  const [schedules, setSchedules] = useState([]);
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [scheduleRes, recordsRes] = await Promise.all([
          scheduleApi.getMySchedule(false),
          collectionRecordApi.getMyRecords(),
        ]);
        setSchedules(scheduleRes.data.data || []);
        setRecords((recordsRes.data.data || []).slice(0, 15));
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

  return (
    <DashboardLayout>
      <PageHeader title="Assigned Route" subtitle="Your full recurring schedule and recent pickup history." />

      <div className="mb-8">
        <h3 className="mb-3 text-sm font-semibold text-ink-700">Recurring Schedule</h3>
        {schedules.length === 0 ? (
          <EmptyState title="No zone assigned" subtitle="Contact your administrator." icon="🗺️" />
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {schedules.map((s) => (
              <div key={s.id} className="card p-5">
                <h4 className="font-semibold text-ink-900">{s.zoneName}</h4>
                <p className="mt-1 text-sm text-ink-500">
                  {formatTime(s.startTime)} – {formatTime(s.endTime)}
                </p>
                <div className="mt-3 flex flex-wrap gap-1.5">
                  {(s.collectionDays || []).map((d) => (
                    <span key={d} className="rounded-full bg-brand-50 px-2 py-0.5 text-xs text-brand-700">
                      {d.slice(0, 3)}
                    </span>
                  ))}
                </div>
                <p className="mt-3 text-xs text-ink-400">Vehicle: {s.vehicleNumber || 'Not assigned'}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      <div>
        <h3 className="mb-3 text-sm font-semibold text-ink-700">Recent Collection History</h3>
        {records.length === 0 ? (
          <EmptyState title="No collections logged yet" icon="🚚" />
        ) : (
          <div className="table-shell">
            <table className="table-base">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Zone</th>
                  <th>Segregation</th>
                  <th>Remarks</th>
                </tr>
              </thead>
              <tbody>
                {records.map((r) => (
                  <tr key={r.id}>
                    <td>{formatDate(r.collectionDate)}</td>
                    <td>{r.zoneName}</td>
                    <td>
                      <Badge status={r.segregationCompliant ? 'RESOLVED' : 'REJECTED'}>
                        {r.segregationCompliant ? 'Compliant' : 'Non-compliant'}
                      </Badge>
                    </td>
                    <td>{r.remarks || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
