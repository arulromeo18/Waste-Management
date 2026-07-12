import React, { useEffect, useMemo, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import { scheduleApi } from '../../api/scheduleApi.js';
import { zoneApi } from '../../api/zoneApi.js';
import { userApi } from '../../api/userApi.js';
import { vehicleApi } from '../../api/vehicleApi.js';
import { DAYS_OF_WEEK } from '../../utils/constants.js';
import { formatTime, formatDate } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function ManageSchedules() {
  const { showToast } = useToast();
  const [zones, setZones] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);

  const [selectedZoneId, setSelectedZoneId] = useState('');
  const [activeSchedule, setActiveSchedule] = useState(null);
  const [history, setHistory] = useState([]);
  const [scheduleLoading, setScheduleLoading] = useState(false);

  const [form, setForm] = useState({
    workerId: '',
    vehicleId: '',
    collectionDays: [],
    startTime: '08:00',
    endTime: '11:00',
  });
  const [saving, setSaving] = useState(false);

  async function loadBase() {
    setLoading(true);
    try {
      const [zonesRes, workersRes, vehiclesRes] = await Promise.all([
        zoneApi.getAll(),
        userApi.getAllWorkers(),
        vehicleApi.getAll(),
      ]);
      const activeZones = (zonesRes.data.data || []);
      setZones(activeZones);
      setWorkers(workersRes.data.data || []);
      setVehicles(vehiclesRes.data.data || []);
      if (activeZones.length > 0) setSelectedZoneId(activeZones[0].id);
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadBase();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadScheduleForZone(zoneId) {
    if (!zoneId) return;
    setScheduleLoading(true);
    try {
      const [activeRes, historyRes] = await Promise.all([
        scheduleApi.getActiveForZoneAdmin(zoneId),
        scheduleApi.getHistoryForZone(zoneId),
      ]);
      const active = activeRes.data.data;
      setActiveSchedule(active);
      setHistory(historyRes.data.data || []);
      setForm({
        workerId: active?.workerId || '',
        vehicleId: active?.vehicleId || '',
        collectionDays: active?.collectionDays || [],
        startTime: active?.startTime || '08:00',
        endTime: active?.endTime || '11:00',
      });
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setScheduleLoading(false);
    }
  }

  useEffect(() => {
    if (selectedZoneId) loadScheduleForZone(selectedZoneId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedZoneId]);

  const zoneWorkers = useMemo(
    () => workers.filter((w) => w.zoneId === selectedZoneId),
    [workers, selectedZoneId]
  );
  const zoneVehicles = useMemo(
    () => vehicles.filter((v) => v.zoneId === selectedZoneId),
    [vehicles, selectedZoneId]
  );

  function toggleDay(day) {
    setForm((prev) => ({
      ...prev,
      collectionDays: prev.collectionDays.includes(day)
        ? prev.collectionDays.filter((d) => d !== day)
        : [...prev.collectionDays, day],
    }));
  }

  async function handleSave(e) {
    e.preventDefault();
    if (form.collectionDays.length === 0) {
      showToast('Select at least one collection day', 'error');
      return;
    }
    setSaving(true);
    const payload = { zoneId: selectedZoneId, ...form };
    try {
      if (activeSchedule) {
        await scheduleApi.update(activeSchedule.id, payload);
        showToast('Schedule updated successfully');
      } else {
        await scheduleApi.create(payload);
        showToast('Schedule created successfully');
      }
      loadScheduleForZone(selectedZoneId);
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <DashboardLayout>
        <Loader />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <PageHeader title="Manage Schedules" subtitle="Zone-wise recurring waste collection schedules." />

      {zones.length === 0 ? (
        <EmptyState title="No zones available" subtitle="Create a zone first." icon="🗺️" />
      ) : (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <div className="card p-5 lg:col-span-1">
            <label className="label">Select zone</label>
            <select
              className="input"
              value={selectedZoneId}
              onChange={(e) => setSelectedZoneId(e.target.value)}
            >
              {zones.map((z) => (
                <option key={z.id} value={z.id}>
                  {z.zoneName}
                </option>
              ))}
            </select>

            {scheduleLoading ? (
              <Loader />
            ) : (
              <form onSubmit={handleSave} className="mt-5 space-y-4">
                <div>
                  <label className="label">Assigned worker</label>
                  <select
                    className="input"
                    value={form.workerId}
                    onChange={(e) => setForm({ ...form, workerId: e.target.value })}
                  >
                    <option value="">Unassigned</option>
                    {zoneWorkers.map((w) => (
                      <option key={w.id} value={w.id}>
                        {w.fullName}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="label">Assigned vehicle</label>
                  <select
                    className="input"
                    value={form.vehicleId}
                    onChange={(e) => setForm({ ...form, vehicleId: e.target.value })}
                  >
                    <option value="">Unassigned</option>
                    {zoneVehicles.map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.vehicleNumber}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="label">Collection days</label>
                  <div className="flex flex-wrap gap-2">
                    {DAYS_OF_WEEK.map((day) => (
                      <button
                        type="button"
                        key={day}
                        onClick={() => toggleDay(day)}
                        className={`rounded-full px-3 py-1.5 text-xs font-medium ${
                          form.collectionDays.includes(day)
                            ? 'bg-brand-600 text-white'
                            : 'bg-ink-100 text-ink-600'
                        }`}
                      >
                        {day.slice(0, 3)}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="label">Start time</label>
                    <input
                      type="time"
                      className="input"
                      value={form.startTime}
                      onChange={(e) => setForm({ ...form, startTime: e.target.value })}
                    />
                  </div>
                  <div>
                    <label className="label">End time</label>
                    <input
                      type="time"
                      className="input"
                      value={form.endTime}
                      onChange={(e) => setForm({ ...form, endTime: e.target.value })}
                    />
                  </div>
                </div>
                <button type="submit" disabled={saving} className="btn-primary w-full">
                  {saving ? 'Saving…' : activeSchedule ? 'Update Schedule' : 'Create Schedule'}
                </button>
              </form>
            )}
          </div>

          <div className="lg:col-span-2">
            <h3 className="mb-3 text-sm font-semibold text-ink-700">Schedule history for this zone</h3>
            {history.length === 0 ? (
              <EmptyState title="No schedule history" subtitle="Create a schedule to see it here." icon="📅" />
            ) : (
              <div className="table-shell">
                <table className="table-base">
                  <thead>
                    <tr>
                      <th>Days</th>
                      <th>Time</th>
                      <th>Worker</th>
                      <th>Vehicle</th>
                      <th>Status</th>
                      <th>Created</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.map((s) => (
                      <tr key={s.id}>
                        <td>{s.collectionDays?.map((d) => d.slice(0, 3)).join(', ')}</td>
                        <td>
                          {formatTime(s.startTime)} – {formatTime(s.endTime)}
                        </td>
                        <td>{s.workerName || '—'}</td>
                        <td>{s.vehicleNumber || '—'}</td>
                        <td>
                          <Badge status={s.active ? 'ACTIVE' : 'INACTIVE'}>
                            {s.active ? 'Active' : 'Superseded'}
                          </Badge>
                        </td>
                        <td>{formatDate(s.createdAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}
